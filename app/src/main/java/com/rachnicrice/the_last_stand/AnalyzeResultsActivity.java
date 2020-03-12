package com.rachnicrice.the_last_stand;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Objects;

public class AnalyzeResultsActivity extends AppCompatActivity {

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_results);

        final String TAG = "rnr.Analyze";

        Handler handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                // get the times from bother users
                DatabaseReference users = database.getReference("users");
                users.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        handleUserEvent(dataSnapshot, TAG);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        handleUserEvent(dataSnapshot, TAG);
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
                });
            }
        };
        handler.postDelayed(r, 5000);
    }

    private void handleUserEvent (DataSnapshot dataSnapshot, String TAG) {
        Intent i = getIntent();
        long myTime = i.getLongExtra("time", -1);
        String myID = i.getStringExtra("my_id");
        String enemyID = i.getStringExtra("enemy_id");

        if(Objects.equals(dataSnapshot.getKey(), enemyID)) {
            if(dataSnapshot.getValue(Long.class) != null) {
                long enemyTime = dataSnapshot.getValue(Long.class);
                Date myDate = new Date(myTime);
                Date enemyDate = new Date(enemyTime);
                // Intent to trigger on result
                Intent intent = new Intent(getApplicationContext(),
                        ResultActivity.class);
                // make sure enemy time is within the same minute
                if(myDate.getYear() == enemyDate.getYear() &&
                        myDate.getMonth() == enemyDate.getMonth() &&
                        myDate.getDate() == enemyDate.getDate() &&
                        myDate.getHours() == enemyDate.getHours() &&
                        myDate.getMinutes() == enemyDate.getMinutes()) {

                    // determine who won
                    int timeDiff = myDate.compareTo(enemyDate);
                    if(timeDiff < 0) {
                        // I won!
                        Log.i(TAG, myID + " defeats " + enemyID);

                        /********************************************
                         * need to change something in the db to
                         * reflect enemy is out
                         *******************************************/

                        // move to results page with me as the winner
                        intent.putExtra("eliminated", "");
                        startActivity(intent);
                    }
                    else if(timeDiff > 0) {
                        // the enemy won :-(
                        Log.i(TAG, enemyID + " defeats " + myID);

                        /********************************************
                         * need to change something in the db to
                         * reflect I am out
                         *******************************************/

                        // move to results page with enemy as winner
                        // move to results page with me as the winner
                        intent.putExtra("eliminated", "eliminated");
                        startActivity(intent);
                    } else {
                        // there was a tie
                        Log.i(TAG, "There was a tie between " + myID +
                                " and " + enemyID);

                        /********************************************
                         * I'm not really sure how to handle this
                         *******************************************/

                    }
                } else {
                    // I must be the winner
                    // move to results page with me as the winner
                    intent.putExtra("eliminated", "");
                    startActivity(intent);
                }
            }
        }
    }
}
