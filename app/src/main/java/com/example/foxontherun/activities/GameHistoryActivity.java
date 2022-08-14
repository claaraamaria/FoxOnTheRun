package com.example.foxontherun.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.example.foxontherun.R;

public class GameHistoryActivity extends AppCompatActivity {

    private TextView historyTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        historyTextview = findViewById(R.id.historyText);
    }
}