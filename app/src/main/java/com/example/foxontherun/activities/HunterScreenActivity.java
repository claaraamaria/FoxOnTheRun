package com.example.foxontherun.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.foxontherun.R;

public class HunterScreenActivity extends AppCompatActivity {

    private View hotColdCircle;
    private TextView hotColdText, distanceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunter_screen);

        hotColdCircle = findViewById(R.id.hotColdCircle);
        hotColdText = findViewById(R.id.hotCold);
        distanceText = findViewById(R.id.distance);
    }
}