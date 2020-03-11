package com.rachnicrice.the_last_stand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HowToPlay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        //navigate home on home button push
        View home = findViewById(R.id.home);
        home.setOnClickListener((v) -> {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        });
    }
}
