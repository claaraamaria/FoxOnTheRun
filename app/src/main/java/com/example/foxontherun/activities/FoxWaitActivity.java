package com.example.foxontherun.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.foxontherun.R;

public class FoxWaitActivity extends AppCompatActivity {

    private TextView role, countdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fox_wait);

        role = findViewById(R.id.roleHolder);
        countdown = findViewById(R.id.countdown_text);
    }
}