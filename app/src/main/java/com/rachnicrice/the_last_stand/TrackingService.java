package com.rachnicrice.the_last_stand;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TrackingService extends Service {

//    Referenced from https://www.androidauthority.com/create-a-gps-tracking-application-with-firebase-realtime-databse-844343/
    private static final String TAG = "rnr";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) builder.setChannelId(youChannelID);
//        buildNotification();
        requestLocationUpdates();
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
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);

                        Location location = locationResult.getLastLocation();


                        if (location != null) {
                            //Save the location data to the database//
                            ref.setValue(location);
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
    //reference used: https://rosettacode.org/wiki/Haversine_formula#Java

    public static double earthRadius = 6372.8; // earth radius in kilometers

    public static double distanceCalc(double userlat1, double userlon1, double userlat2, double userlon2) {
        double latDiff = Math.toRadians(userlat1 - userlat2);
        double lonDiff = Math.toRadians(userlon1 - userlon2);
        userlat1 = Math.toRadians(userlat1);
        userlat2 = Math.toRadians(userlat2);

        double a = Math.pow(Math.sin(latDiff / 2),2) + Math.pow(Math.sin(lonDiff / 2),2) * Math.cos(userlat1) * Math.cos(userlat2);
        double c = 2 * Math.asin(Math.sqrt(a));

        return earthRadius * c;
    }

}
