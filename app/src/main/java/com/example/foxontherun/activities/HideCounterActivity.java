package com.example.foxontherun.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foxontherun.R;
import com.example.foxontherun.model.DistanceDTO;
import com.example.foxontherun.model.GameConfiguration;
import com.example.foxontherun.model.LocationDTO;
import com.example.foxontherun.model.Player;
import com.example.foxontherun.server.RESTClient;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HideCounterActivity extends AppCompatActivity {

    private TextView role, countdownText;

    private CountDownTimer countDownTimer;
    private long timeLeftMilliseconds;
    private boolean timerRunning;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateGameState();
            handler.postDelayed(runnable, delay);
        }
    };
    private int delay = 3000;
    private Boolean handlerStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_counter);

        this.role = findViewById(R.id.roleHolder);
        this.countdownText = findViewById(R.id.countdown_text);

        calculateTimeLeft();
        getRole();
    }

    public void calculateTimeLeft() {
        Call<Date> getStartDateCall = RESTClient
                .getInstance()
                .getApi()
                .getStartDate(Player.getGlobalRoomName());

        getStartDateCall.enqueue(new Callback<Date>() {
            @Override
            public void onResponse(Call<Date> call, Response<Date> response) {
                Long startDateTime = response.body().getTime();
                setStartDateTime(startDateTime);
            }

            @Override
            public void onFailure(Call<Date> call, Throwable t) {
                //can't go wrong
            }
        });
    }

    private void setStartDateTime(Long startDateTime) {
        this.timeLeftMilliseconds = startDateTime +
                GameConfiguration.getHideTimer() * 1000 + 4870 -
                Calendar.getInstance().getTimeInMillis();
        startStopTimerFE();
    }

    private void startStopTimerFE() {
        if (timerRunning) {
            stopTimer();
        } else {
            startTimerFE();
        }
    }

    private void stopTimer() {
        countDownTimer.cancel();
        timerRunning = false;
    }

    private void startTimerFE() {
        countDownTimer = new CountDownTimer(timeLeftMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMilliseconds = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                if(timerRunning) {
                    stopTimer();
                }
                countdownText.setText("The game will start very soon");
                if(!handlerStarted) {
                    startCallHandler();
                }
            }
        }.start();
        timerRunning = true;
    }

    private void updateTimer() {
        int minutes = (int) timeLeftMilliseconds / 60000;
        int seconds = (int) timeLeftMilliseconds % 60000 / 1000;

        String timeLeftText;
        timeLeftText = "" + minutes;
        timeLeftText += ":";
        if (seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;

        countdownText.setText(timeLeftText);

        if((timeLeftMilliseconds / 1000) <= 30 && !this.handlerStarted) { //cand raman 30 de sec
            startCallHandler();
        }
    }

    void startCallHandler() {
        this.handlerStarted = true;
        handler.postDelayed(runnable, delay);
    }

    private void getRole() {
        if (Player.getGlobalRole()) {
            this.role.setText("Hunter");
        } else {
            this.role.setText("Fox");
        }
    }

    private void updateGameState() {
        LocationDTO playerLocation = new LocationDTO(Player.getGlobalName());
        Call<DistanceDTO> gameStateCall = RESTClient
                .getInstance()
                .getApi()
                .updateLocation(Player.getGlobalRoomName(), playerLocation);

        gameStateCall.enqueue(new Callback<DistanceDTO>() {
            @Override
            public void onResponse(Call<DistanceDTO> call, Response<DistanceDTO> response) {
                Integer gameStateResult = response.body().getGameState();

                if (gameStateResult == 2) {
                    handler.removeCallbacks(runnable);
                    finish();
                    if (Player.getGlobalRole()) {
                        startActivity(new Intent(HideCounterActivity.this, HunterScreenActivity.class));
                    } else {
                        startActivity(new Intent(HideCounterActivity.this, FoxScreenActivity.class));
                    }
                }
            }

            @Override
            public void onFailure(Call<DistanceDTO> call, Throwable t) {
                handler.removeCallbacks(runnable);
                finish();
                Toast.makeText(HideCounterActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}