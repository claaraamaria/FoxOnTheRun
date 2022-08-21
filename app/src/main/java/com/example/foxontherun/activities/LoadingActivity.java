package com.example.foxontherun.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foxontherun.R;
import com.example.foxontherun.model.DistanceDTO;
import com.example.foxontherun.model.LocationDTO;
import com.example.foxontherun.model.Player;
import com.example.foxontherun.server.RESTClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView loadingNumber;

    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftMilliseconds = 300000; // 5 sec
    //public static final int PROGRESSBAR_DURATION = 18; // 3 min
    public static final int PROGRESSBAR_DURATION = 6; // 1 min


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        progressBar = findViewById(R.id.progressBar);
        loadingNumber = findViewById(R.id.loadingText);

        progressBar.setMax(100);
        progressBar.setScaleY(3f);

        progressAnimation();

        startStopTimer();

    }

    private void startStopTimer() {
        if (timerRunning) {
            //stopTimer();
        } else {
            startTimer();
        }
    }

    private void stopTimer() {
        countDownTimer.cancel();
        timerRunning = false;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftMilliseconds, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMilliseconds = millisUntilFinished;
                assignRole();
            }

            @Override
            public void onFinish() {
                Toast.makeText(LoadingActivity.this, "Something went wrong with BE timer!", Toast.LENGTH_SHORT).show();
            }
        }.start();
        timerRunning = true;
    }

    private void assignRole() {
        LocationDTO playerLocation = new LocationDTO(Player.getGlobalName());
        Call<DistanceDTO> gameStateCall = RESTClient
                .getInstance()
                .getApi()
                .updateLocation(Player.getGlobalRoomName(), playerLocation);

        Call<Boolean> roleAssignCall = RESTClient
                .getInstance()
                .getApi()
                .getRole(Player.getGlobalRoomName(), Player.getGlobalName());

        gameStateCall.enqueue(new Callback<DistanceDTO>() {
            @Override
            public void onResponse(Call<DistanceDTO> call, Response<DistanceDTO> response) {
                progressAnimation();
                Integer gameStateResult = response.body().getGameState();

                if (gameStateResult == 1) {
                    roleAssignCall.enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                            Boolean roleAssignResult = response.body();
                            Player.setGlobalRole(roleAssignResult);
                            stopTimer();
                            startActivity(new Intent(LoadingActivity.this, FoxWaitActivity.class));
                        }
                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {
                            Toast.makeText(LoadingActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DistanceDTO> call, Throwable t) {
                System.out.println(t.getMessage());
                Toast.makeText(LoadingActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void progressAnimation() {
        ProgressBarAnimation animation = new ProgressBarAnimation(this, progressBar, loadingNumber, 0f, 100f);
        animation.setDuration(10000 * PROGRESSBAR_DURATION);

        progressBar.setAnimation(animation);
    }
}