package com.rachnicrice.the_last_stand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    SharedPreferences p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String result = p.getString("eliminated", "");
        String myTeam = p.getString("my_team", "");

        ImageView image = findViewById(R.id.resultImage);
        TextView text = findViewById(R.id.resultText);

        if (result.equals("eliminated")) {
            if (myTeam.equals("dragons")) {
                image.setImageResource(R.drawable.knight);
                text.setText("You have been slain!");
            } else  {
                image.setImageResource(R.drawable.dragon_fire);
                text.setText("You have been defeated.");
            }
        } else {
            if (myTeam.equals("dragons")) {
                image.setImageResource(R.drawable.dragon_fire);
                text.setText("The knight has been slain!");
            } else  {
                image.setImageResource(R.drawable.knight);
                text.setText("You have slain the dragon!");
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
