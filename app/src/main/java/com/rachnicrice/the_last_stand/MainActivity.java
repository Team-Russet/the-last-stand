package com.rachnicrice.the_last_stand;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "rnr.Main";
    private static final int PERMISSIONS_REQUEST = 100;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    SharedPreferences p;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        database = FirebaseDatabase.getInstance();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences.Editor edit = p.edit();
        edit.putBoolean("tracking_enabled", true);
        edit.apply();

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build());

        //change text view to team name
        String teamName = p.getString("my_team", "default");
        TextView homePageTitle = findViewById(R.id.teamName);

        if(!teamName.equals("default")){
            String newText = "Team: " + teamName;
            homePageTitle.setText(newText);
        }

        //navigate to how to play page on how to play button push
        View howToPlay = findViewById(R.id.howToPlay);
        howToPlay.setOnClickListener((v) -> {
            Intent intent = new Intent(this, HowToPlay.class);
            startActivity(intent);
        });

        // Create and launch sign-in intent
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.v(TAG, "The user is already logged in!");
        }else{
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    202);
        };

        //Grab the log out button
        Button logout = findViewById(R.id.logout);

        //When the log out button is pressed, log the user out
        logout.setOnClickListener((v) -> {
            AuthUI.getInstance()
                    .signOut(this);
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    202);
        });

        //Referenced from https://www.androidauthority.com/create-a-gps-tracking-application-with-firebase-realtime-databse-844343/
        //Check whether GPS tracking is enabled//
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check whether this app has access to the location permission//
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //If the location permission has been granted, then start the TrackerService//
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {

        //If the permission has been granted...//
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        //...then start the GPS tracking service//
            startTrackerService();
        } else {

        //If the user denies the permission request, then display a toast with some more information//
            Toast.makeText(this, "Please enable location services to allow GPS tracking", Toast.LENGTH_SHORT).show();
        }
    }

    //Start the TrackerService//
    private void startTrackerService() {

        Log.i(TAG, "in startTrackingService, eliminated = " + p.getBoolean("eliminated", false));
        Log.i(TAG, "in startTrackingService, tracking_enabled = " + p.getBoolean("tracking_enabled", false));

        if (!p.getBoolean("eliminated", false) &&
                p.getBoolean("tracking_enabled", false)) {
            Log.i(TAG, "starting TrackingService");
            startService(new Intent(this, TrackingService.class));

            SharedPreferences.Editor edit = p.edit();
            edit.putBoolean("tracking_enabled", true);
            edit.apply();

            //Notify the user that tracking has been enabled//
            Toast.makeText(this, "GPS tracking enabled", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "not starting TrackingService");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 202) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = mAuth.getCurrentUser();
                String uid = user.getUid();

                //Check whether this app has access to the location permission//
                int permission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

                //If the location permission has been granted, then start the TrackerService//
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    startTrackerService();
                } else {

                    //If the app doesn’t currently have access to the user’s location, then request access//
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST);
                }

                // grab all teams from db
                DatabaseReference teamRef = database.getReference("teams");

                teamRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // boolean for tracking if user on a team
                        boolean isOnTeam = false;
                        // knights team size counter
                        int knightsSize = 0;
                        // dragons team size counter
                        int dragonsSize = 0;
                        SharedPreferences.Editor edit = p.edit();

                        // get the teams
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        // loop through the teams
                        for(DataSnapshot team : children) {
                            for(DataSnapshot user : team.getChildren()) {
                                // increment team size count
                                if(team.getKey().equals("knights")) {
                                    knightsSize++;
                                    // check to see if user on team
                                    if(uid.equals(user.getKey())) {
                                        isOnTeam = true;
                                        edit.putString("enemy_team", "dragons");
                                        edit.putString("my_team", "knights");
                                        edit.putBoolean("eliminated", !user.getValue(Boolean.class));
                                        edit.apply();

                                        //change text view to team name
                                        TextView homePageTitle = findViewById(R.id.teamName);
                                        String newText = "Team: Knights";
                                        homePageTitle.setText(newText);

                                        Log.d(TAG, "Logged in as " + uid +
                                                ". My team: " + p.getString("my_team", "") +
                                                ". Enemy team: " + p.getString("enemy_team", ""));
                                    }
                                } else {
                                    dragonsSize++;
                                    // check to see if user on team
                                    if(uid.equals(user.getKey())) {
                                        isOnTeam = true;
                                        edit.putString("enemy_team", "knights");
                                        edit.putString("my_team", "dragons");
                                        edit.putBoolean("eliminated", !user.getValue(Boolean.class));
                                        edit.apply();

                                        //change text view to team name
                                        TextView homePageTitle = findViewById(R.id.teamName);
                                        String newText = "Team: Dragons";
                                        homePageTitle.setText(newText);

                                        Log.d(TAG, "Logged in as " + uid +
                                                ". My team: " + p.getString("my_team", "") +
                                                ". Enemy team: " + p.getString("enemy_team", ""));
                                    }
                                }
                            }
                        }

                        // if isOnTeam is false, add user to smallest team
                        if(!isOnTeam) {
                            if(knightsSize > dragonsSize) {
                                // add user to team dragons
                                final String dragonsPath = "teams/dragons/" + uid;
                                DatabaseReference dragonsRef = database.getReference(dragonsPath);
                                dragonsRef.setValue(true);

                                edit.putString("enemy_team", "knights");
                                edit.putString("my_team", "dragons");
                                edit.apply();

                                //change text view to team name
                                TextView homePageTitle = findViewById(R.id.teamName);
                                String newText = "Team: Dragons";
                                homePageTitle.setText(newText);

                                Log.d(TAG, "Logged in as " + uid +
                                        ". My team: " + p.getString("my_team", "") +
                                        ". Enemy team: " + p.getString("enemy_team", ""));
                            } else {
                                // add user to team knights
                                final String knightsPath = "teams/knights/" + uid;
                                DatabaseReference knightsRef = database.getReference(knightsPath);
                                knightsRef.setValue(true);

                                edit.putString("enemy_team", "dragons");
                                edit.putString("my_team", "knights");
                                edit.apply();

                                //change text view to team name
                                TextView homePageTitle = findViewById(R.id.teamName);
                                String newText = "Team: Knights";
                                homePageTitle.setText(newText);

                                Log.d(TAG, "Logged in as " + uid +
                                        ". My team: " + p.getString("my_team", "") +
                                        ". Enemy team: " + p.getString("enemy_team", ""));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } else {
                Log.i(TAG, "Sign in failed!");
            }
        }
    }
}
