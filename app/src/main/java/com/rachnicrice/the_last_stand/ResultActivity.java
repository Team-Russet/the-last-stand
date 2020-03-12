package com.rachnicrice.the_last_stand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    final String TAG = "rnr.Results";
    SharedPreferences p;
    MediaPlayer victory;
    MediaPlayer defeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean result = p.getBoolean("eliminated", false);
        Log.d(TAG, "result: " + result);
        String myTeam = p.getString("my_team", "");

        victory = MediaPlayer.create(this, R.raw.final_battle);
        defeat = MediaPlayer.create(this, R.raw.bcc);

        ImageView image = findViewById(R.id.resultImage);
        TextView text = findViewById(R.id.resultText);
        Button home = findViewById(R.id.home);

        home.setOnClickListener((v) -> {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        });

        if (result) {
            if (myTeam.equals("dragons")) {
                image.setImageResource(R.drawable.knight);
                text.setText(R.string.eliminated_dragon);
            } else  {
                image.setImageResource(R.drawable.dragon_fire);
                text.setText(R.string.eliminated_knight);
            }
            defeat.start();
        } else {
            if (myTeam.equals("dragons")) {
                image.setImageResource(R.drawable.dragon_fire);
                text.setText(R.string.dragon_wins);
            } else  {
                image.setImageResource(R.drawable.knight);
                text.setText(R.string.knight_wins);
            }
            victory.start();
        }

        //https://stackoverflow.com/questions/16035328/how-to-close-activity-after-x-minutes
        Handler finishTaskHandler = new Handler();
        Runnable finishTask = new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        };

        finishTaskHandler.postDelayed(finishTask, 15000);
    }

    @Override
    public void onPause () {
        super.onPause();

        victory.pause();
        defeat.pause();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();

        victory.release();
        defeat.release();
    }
}
