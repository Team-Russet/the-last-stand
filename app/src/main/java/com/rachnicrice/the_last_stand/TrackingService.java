package com.rachnicrice.the_last_stand;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

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

public class TrackingService extends Service {

//    Referenced from https://www.androidauthority.com/create-a-gps-tracking-application-with-firebase-realtime-databse-844343/
    private static final String TAG = "rnr";
    FirebaseDatabase database;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.setChannelId(youChannelID);
//        buildNotification();
        database = FirebaseDatabase.getInstance();

        requestLocationUpdates();
        compareUserLocations();
    }

    //Initiate the request to track the device's location//
    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();

        //Specify how often your app should request the device’s location//
        request.setInterval(10000);

        //Get the user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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
                            //Save the location data to the database//
                            latRef.setValue(location.getLatitude());
                            lonRef.setValue(location.getLongitude());
                        }
                    }
                }, null);
            }
        } else {
            Log.i(TAG, "No one's signed in!");
        }
    }

    private void compareUserLocations() {
        DatabaseReference location = database.getReference("location");

        ChildEventListener locationChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "new location added: " + dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "location changed: " + dataSnapshot.getKey());
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
}
