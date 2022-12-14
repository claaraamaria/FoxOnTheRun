package com.example.foxontherun.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.PowerManager;
import android.view.Menu;
import android.view.View;
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

public class HunterScreenActivity extends AppCompatActivity implements SensorEventListener {

    private View hotColdCircle;
    private View orientationCursor;
    private TextView hotColdText, distanceText, countdownText;

    private CountDownTimer countDownTimer;
    private long timeLeftMilliseconds;
    private boolean timerRunning;

    public static final int UPDATE_INTERVAL = 1;
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hunter_screen);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        hotColdCircle = findViewById(R.id.hotColdCircle);
        orientationCursor = findViewById(R.id.orientationCursor);
        hotColdText = findViewById(R.id.hotCold);
        distanceText = findViewById(R.id.distance);
        countdownText = findViewById(R.id.countdownText);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        locationRequest = LocationRequest.create()
                .setInterval(1000 * UPDATE_INTERVAL)
                .setFastestInterval(1000 * UPDATE_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        //event that is triggered whenever the time interval is met
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                double playerLongitude = locationResult.getLastLocation().getLongitude();
                double playerLatitude = locationResult.getLastLocation().getLatitude();
                double playerAltitude = locationResult.getLastLocation().getAltitude();

                Float phoneAzimuth = getOrientationAngles();

                LocationDTO locationDTO = new LocationDTO(Player.getGlobalName(),
                        playerLatitude, playerLongitude, playerAltitude, phoneAzimuth);

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
                            Toast.makeText(HunterScreenActivity.this, "Fox WON!", Toast.LENGTH_SHORT).show();
                            stopLocationUpdates();
                            finish();
                            Player.setGlobalRoomName(null);
                            startActivity(new Intent(HunterScreenActivity.this, HomeScreenActivity.class));

                        } else if (gameStateResult == 5) {
                            Toast.makeText(HunterScreenActivity.this, "You WON!", Toast.LENGTH_SHORT).show();
                            stopLocationUpdates();
                            finish();
                            Player.setGlobalRoomName(null);
                            startActivity(new Intent(HunterScreenActivity.this, HomeScreenActivity.class));

                        }

                        Double distance = response.body().getDistance();

                        if (distance == -1) {
                            //waiting for he fox to send location
                            //or go to next call (denotes the last one)
                            return;
                        } else if (distance < 11) {
                            //hot
                            hotColdCircle.getBackground().setColorFilter(getColor(R.color.hot), PorterDuff.Mode.SRC_ATOP);
                            hotColdText.setText("HOT");
                            distanceText.setText("Between 11 and 22 meters");
                        } else if (distance < 22) {
                            //warm
                            hotColdCircle.getBackground().setColorFilter(getColor(R.color.warm), PorterDuff.Mode.SRC_ATOP);
                            hotColdText.setText("WARM");
                            distanceText.setText("Between 22 and 33 meters");
                        } else if (distance < 33) {
                            //cold
                            hotColdCircle.getBackground().setColorFilter(getColor(R.color.cold), PorterDuff.Mode.SRC_ATOP);
                            hotColdText.setText("COLD");
                            distanceText.setText("Between 33 and 44 meters");
                        } else if (distance < 44) {
                            //verycold
                            hotColdCircle.getBackground().setColorFilter(getColor(R.color.very_cold), PorterDuff.Mode.SRC_ATOP);
                            hotColdText.setText("VERY COLD");
                            distanceText.setText("Between 44 and 55 meters");
                        } else if (distance > 55) {
                            hotColdCircle.getBackground().setColorFilter(getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                            hotColdText.setTextColor(getColor(R.color.white));
                            hotColdText.setText("OUT OF BOUNDS");
                            distanceText.setText("More than 55 meters");
                        }

                        Double angle = response.body().getAngle();

                        if(angle <= 30) {
                            orientationCursor.getBackground().setColorFilter(getColor(R.color.hot), PorterDuff.Mode.SRC_ATOP);
                        } else {
                            orientationCursor.getBackground().setColorFilter(getColor(R.color.very_cold), PorterDuff.Mode.SRC_ATOP);
                        }
                    }

                    @Override
                    public void onFailure(Call<DistanceDTO> call, Throwable t) {
                        stopLocationUpdates();
                        finish();
                        Toast.makeText(HunterScreenActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        configureGPS();
        calculateTimeLeft();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do something if sensor accuracy changes
    }

    public Float getOrientationAngles() {
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        float[] orientation = SensorManager.getOrientation(rotationMatrix, orientationAngles);

        return orientation[0];
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        sensorManager.unregisterListener(this);
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