package com.rachnicrice.the_last_stand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    SharedPreferences p;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String result = p.getString("eliminated", "");
        String myTeam = p.getString("my_team", "");

        player = MediaPlayer.create(this, R.raw.lobo);

        ImageView image = findViewById(R.id.resultImage);
        TextView text = findViewById(R.id.resultText);

        if (result.equals("eliminated")) {
            if (myTeam.equals("dragons")) {
                image.setImageResource(R.drawable.knight);
                text.setText(R.string.eliminated_dragon);
            } else  {
                image.setImageResource(R.drawable.dragon_fire);
                text.setText(R.string.eliminated_knight);
            }
        } else {
            if (myTeam.equals("dragons")) {
                image.setImageResource(R.drawable.dragon_fire);
                text.setText(R.string.dragon_wins);
            } else  {
                image.setImageResource(R.drawable.knight);
                text.setText(R.string.knight_wins);
            }
        }

        //https://stackoverflow.com/questions/16035328/how-to-close-activity-after-x-minutes
        Handler finishTaskHandler = new Handler();
        Runnable finishTask = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };

        finishTaskHandler.postDelayed(finishTask, 10000);
    }
}
