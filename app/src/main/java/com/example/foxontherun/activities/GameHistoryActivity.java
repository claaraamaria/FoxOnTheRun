package com.example.foxontherun.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.foxontherun.R;

public class GameHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        TextView historyTextview = findViewById(R.id.historyText);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}