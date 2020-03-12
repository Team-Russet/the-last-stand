package com.rachnicrice.the_last_stand;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class TrackingService extends Service {

//    Referenced from https://www.androidauthority.com/create-a-gps-tracking-application-with-firebase-realtime-databse-844343/
    private static final String TAG = "rnr.Tracking";
    FirebaseDatabase database;
    FirebaseUser user;
    double userLatitude = 0;
    double userLongitutde = 0;
    String enemyTeam;
    String myTeam;
    SharedPreferences p;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "TrackingService Created", Toast.LENGTH_SHORT).show();
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.setChannelId(youChannelID);
//        buildNotification();
        database = FirebaseDatabase.getInstance();

        p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //Get the user
        user = FirebaseAuth.getInstance().getCurrentUser();

        requestLocationUpdates();
        compareUserLocations();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "TrackingService Destroyed", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    //Initiate the request to track the device's location//
    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();

        //Specify how often your app should request the device’s location//

        request.setInterval(1000);

        if (user != null) {
            String uid = user.getUid();
            Log.i(TAG, uid);

            //Get the most accurate location data available//
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
            final String path = "location/" + uid;
            int permission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);

            //If the app currently has access to the location permission...//
            if (permission == PackageManager.PERMISSION_GRANTED) {

                //...then request location updates//
                client.requestLocationUpdates(request, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        //Get a reference to the database, so your app can perform read and write operations//
                        DatabaseReference latRef = database.getReference(path + "/latitude");
                        DatabaseReference lonRef = database.getReference(path + "/longitude");

                        Location location = locationResult.getLastLocation();


                        if (location != null) {
                            userLatitude = location.getLatitude();
                            userLongitutde = location.getLongitude();

                            //Save the location data to the database//
                            latRef.setValue(userLatitude);
                            lonRef.setValue(userLongitutde);
                        }
                    }
                }, null);
            }
        } else {
            Log.i(TAG, "No one's signed in!");
        }

        //for this user, check distance against each other users distance
        //if this user is within .003048km (10 feet) certain distance from another user, check if they are on the same or opposing teams.
        //if they are on opposing teams, trigger an activity

    }

    private void compareUserLocations() {
        DatabaseReference location = database.getReference("location");

        ChildEventListener locationChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                enemyTeam = p.getString("enemy_team", "");
                myTeam = p.getString("my_team", "");
                handleLocationChange(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                enemyTeam = p.getString("enemy_team", "");
                myTeam = p.getString("my_team", "");
                handleLocationChange(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        location.addChildEventListener(locationChildListener);
    }

    private void handleLocationChange (DataSnapshot dataSnapshot) {
        Log.d(TAG, "new location added: " + dataSnapshot.getKey());

        // make sure updated user location is not us
        if(user != null) {
            if(!user.getUid().equals(dataSnapshot.getKey())) {
                Log.i(TAG, "we are not the same user");

                // get id of player with changed location
                String playerID = dataSnapshot.getKey();

                Iterable<DataSnapshot> enemyLocationData = dataSnapshot.getChildren();

                // make sure they are an enemy
                DatabaseReference enemyData = database.getReference("teams/" + enemyTeam + "/" + playerID);
                enemyData.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue(Boolean.class) != null) {
                            if (dataSnapshot.getValue(Boolean.class)) {
                                Log.i(TAG, "we are enemies");

                                String enemyLat = "";
                                String enemyLong = "";

                                for(DataSnapshot value: enemyLocationData) {
                                    if(value.getKey().equals("latitude")){
                                        enemyLat = value.getValue().toString();
                                        Log.i(TAG, "enemy latitude: " + enemyLat);
                                    } else {
                                        enemyLong = value.getValue().toString();
                                        Log.i(TAG, "enemy longitude: " + enemyLong);
                                    }
                                }

                                if(!enemyLat.equals("") && !enemyLong.equals("")) {
                                    // compare user location to updated user location
                                    double distance = distanceCalc(userLatitude, userLongitutde,
                                            Double.parseDouble(enemyLat), Double.parseDouble(enemyLong));

                                    Log.i(TAG, "distance to enemy: " + distance);

                                    distanceHandler(distance, playerID);
                                } else {
                                    Log.i(TAG, "enemy lat or lon was empty");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "databaseError");
                    }
                });
            }
        }
    }

    //reference used: https://rosettacode.org/wiki/Haversine_formula#Java
    public void distanceHandler (double distance, String playerID) {
        if (distance <= 100) {
            DatabaseReference me = database.getReference("teams/" + myTeam + "/" + user.getUid());
            DatabaseReference enemy = database.getReference("teams/" + enemyTeam + "/" + playerID);

            enemy.setValue(false);

            SharedPreferences.Editor edit = p.edit();
            edit.putBoolean("tracking_enabled", false);
            edit.apply();
            stopSelf();

            Intent i = new Intent(this, BattleActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("my_id", user.getUid());
            i.putExtra("enemy_id", playerID);
            startActivity(i);
        }
    }

    // returns feet
    public static double distanceCalc(double userlat1, double userlon1, double userlat2, double userlon2) {
        double earthRadius = 6372.8; // earth radius in kilometers
        double latDiff = Math.toRadians(userlat1 - userlat2);
        double lonDiff = Math.toRadians(userlon1 - userlon2);
        userlat1 = Math.toRadians(userlat1);
        userlat2 = Math.toRadians(userlat2);

        double a = Math.pow(Math.sin(latDiff / 2),2) + Math.pow(Math.sin(lonDiff / 2),2) * Math.cos(userlat1) * Math.cos(userlat2);
        double c = 2 * Math.asin(Math.sqrt(a));

        return earthRadius * c * 3280.84;
    }
}
