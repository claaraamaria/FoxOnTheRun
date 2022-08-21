package com.example.foxontherun.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.example.foxontherun.R;

public class FoxWaitActivity extends AppCompatActivity {

    private TextView role, countdownText;

    private CountDownTimer countDownTimer;
    private long timeLeftMilliseconds = 180000; // 3 min
    private boolean timerRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fox_wait);

        role = findViewById(R.id.roleHolder);
        countdownText = findViewById(R.id.countdown_text);

        startStopTimer();

        updateTimer();
    }

    private void startStopTimer() {
        if (timerRunning) {
           // stopTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMilliseconds = millisUntilFinished;
                //

                updateTimer();
            }

            @Override
            public void onFinish() {
                //call catre backend cu rolul
                //daca e vanator, se deschide HunterScreen Activity
                //daca e vulpe, se deschide FoxScreen Activity
                //startActivity(new Intent(this, ProfileActivity.class));
            }
        }.start();
        timerRunning = true;
    }

//    private void stopTimer() {
//        countDownTimer.cancel();
//        timerRunning = false;
//    }

    private void updateTimer() {
        int minutes = (int) timeLeftMilliseconds / 60000;
        int seconds = (int) timeLeftMilliseconds % 60000 / 1000;

        String timeLeftText;
        timeLeftText = "" + minutes;
        timeLeftText += ":";
        if (seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;

        countdownText.setText(timeLeftText);
    }

}