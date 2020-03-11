package com.rachnicrice.the_last_stand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class BattleActivity extends AppCompatActivity {

    final static String TAG = "rnr.BattleActivity";
    String myID = "";
    String enemyID = "";
    SharedPreferences p;
    String myTeam = "";
    String enemyTeam = "";
    private DatabaseReference mDatabase;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Get the user
        user = FirebaseAuth.getInstance().getCurrentUser();
        Log.i(TAG, "user id: " + user.getUid());

        p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        myTeam = p.getString("my_team", "");
        Log.i(TAG, "my team: " + myTeam);
        enemyTeam = p.getString("enemy_team", "");
        Log.i(TAG, "enemy team: " + enemyTeam);

        // get players' id's
        Intent i = getIntent();
        myID = i.getStringExtra("my_id");
        Log.i(TAG, "my id: " + myID);
        enemyID = i.getStringExtra("enemy_id");
        Log.i(TAG, "enemy id: " + enemyID);


        // change button image to my team's image
        ImageButton imgBtn = this.findViewById(R.id.battle_activity_btn);
        if(myTeam.equals("dragons")) {
            imgBtn.setImageResource(R.drawable.dragon_fire);
        } else {
            imgBtn.setImageResource(R.drawable.knight);
        }

        // set listener for image button
        imgBtn.setOnClickListener(v -> {

            Date date = new Date();
            // add timestamp to db
            mDatabase.child("users").child(myID).setValue(date.getTime());

            // analyze results
            Intent intent = new Intent(getApplicationContext(), AnalyzeResultsActivity.class);
            intent.putExtra("time", date.getTime());
            startActivity(intent);
        });
    }
}
