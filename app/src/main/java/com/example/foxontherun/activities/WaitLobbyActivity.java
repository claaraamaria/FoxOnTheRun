package com.example.foxontherun.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ProgressBar;
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

public class WaitLobbyActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView loadingNumber;

    private Long startDateTime;
    private Boolean getStartDateCallFinished = false;

    Handler handler = new Handler();
    Runnable runnable;
    private int delay = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_lobby);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        progressBar = findViewById(R.id.progressBar);
        loadingNumber = findViewById(R.id.loadingText);

        progressBar.setMax(100);
        progressBar.setScaleY(3f);

        calculateTimeLeft();
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
                Toast.makeText(WaitLobbyActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStartDateTime(Long startDateTime) {
        this.startDateTime = startDateTime;
        this.getStartDateCallFinished = true;
    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {

            @Override
            public void run() {
                assignRole();
                handler.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
    }

    private void assignRole() {
        if (getStartDateCallFinished) {
            progressAnimation(
                    this.startDateTime +
                            GameConfiguration.getWaitLobbyTimer() * 1000 + 4870 -
                            Calendar.getInstance().getTimeInMillis()); //4.87 am observat ca raman pana se schimba screen-ul
            getStartDateCallFinished = false;
        }
        LocationDTO playerLocation = new LocationDTO(Player.getGlobalName());
        Call<DistanceDTO> gameStateCall = RESTClient
                .getInstance()
                .getApi()
                .updateLocation(Player.getGlobalRoomName(), playerLocation);

        gameStateCall.enqueue(new Callback<DistanceDTO>() {
            @Override
            public void onResponse(Call<DistanceDTO> call, Response<DistanceDTO> response) {
                Integer gameStateResult = response.body().getGameState();

                if (gameStateResult == 1) {
                    Call<Boolean> roleAssignCall = RESTClient
                            .getInstance()
                            .getApi()
                            .getRole(Player.getGlobalRoomName(), Player.getGlobalName());

                    roleAssignCall.enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                            Boolean roleAssignResult = response.body();
                            Player.setGlobalRole(roleAssignResult);
                            handler.removeCallbacks(runnable);
                            finish();
                            startActivity(new Intent(WaitLobbyActivity.this, HideCounterActivity.class));
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {
                            Toast.makeText(WaitLobbyActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DistanceDTO> call, Throwable t) {
                System.out.println(t.getMessage());
                Toast.makeText(WaitLobbyActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void progressAnimation(Long timeLeftMs) {
        ProgressBarAnimation animation = new ProgressBarAnimation(this, progressBar, loadingNumber, 0f, 100f);
        animation.setDuration(timeLeftMs);

        progressBar.startAnimation(animation);
    }
}