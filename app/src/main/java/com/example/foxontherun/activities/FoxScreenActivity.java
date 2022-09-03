package com.example.foxontherun.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foxontherun.R;
import com.example.foxontherun.model.DistanceDTO;
import com.example.foxontherun.model.GameConfiguration;
import com.example.foxontherun.model.LocationDTO;
import com.example.foxontherun.model.Player;
import com.example.foxontherun.server.RESTClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoxScreenActivity extends AppCompatActivity {

    public static final int UPDATE_INTERVAL = 1;
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    private TextView countdownText;

    private CountDownTimer countDownTimer;
    private long timeLeftMilliseconds;
    private boolean timerRunning;

    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fox_screen);

        countdownText = findViewById(R.id.countdownText);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        locationRequest = LocationRequest.create()
                .setInterval(1000 * UPDATE_INTERVAL)
                .setFastestInterval(1000 * UPDATE_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        //event that is triggered whenever the time interval is met
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                System.out.println("IS LOCATION AVAILABLE ? -> " + locationAvailability.isLocationAvailable());
            }

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if(locationResult.getLastLocation() == null) {
                    return;
                }

                double playerLongitude = locationResult.getLastLocation().getLongitude();
                double playerLatitude = locationResult.getLastLocation().getLatitude();
                double playerAltitude = locationResult.getLastLocation().getAltitude();

                LocationDTO locationDTO = new LocationDTO(Player.getGlobalName(),
                        playerLatitude, playerLongitude, playerAltitude, 0f);

                Call<DistanceDTO> distanceDTOCall = RESTClient
                        .getInstance()
                        .getApi()
                        .updateLocation(Player.getGlobalRoomName(), locationDTO);

                distanceDTOCall.enqueue(new Callback<DistanceDTO>() {
                    @Override
                    public void onResponse(Call<DistanceDTO> call, Response<DistanceDTO> response) {
                        if(response.body() == null) {
                            return;
                        }
                        Integer gameStateResult = response.body().getGameState();

                        if (gameStateResult == 4) {
                            Toast.makeText(FoxScreenActivity.this, "You WON!", Toast.LENGTH_SHORT).show();
                            stopLocationUpdates();
                            finish();
                            startActivity(new Intent(FoxScreenActivity.this, HomeScreenActivity.class));

                        } else if (gameStateResult == 5) {
                            Toast.makeText(FoxScreenActivity.this, "Hunters WON!", Toast.LENGTH_SHORT).show();
                            stopLocationUpdates();
                            finish();
                            startActivity(new Intent(FoxScreenActivity.this, HomeScreenActivity.class));

                        }
                    }

                    @Override
                    public void onFailure(Call<DistanceDTO> call, Throwable t) {
                        Toast.makeText(FoxScreenActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        configureGPS();
        calculateTimeLeft();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    configureGPS();
                } else {
                    Toast.makeText(this, "Permission must be granted in order to function!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void configureGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //we got permissions. Put the values of location. xxx into the UI components
                    if (location != null) {
                        startLocationUpdates();
                    }
                }
            });
        } else {
            //permissions not granted yet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
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
                GameConfiguration.getGameOnTimer() * 1000 + 4870 -
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}