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
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foxontherun.R;
import com.example.foxontherun.model.DistanceDTO;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HunterScreenActivity extends AppCompatActivity {

    private View hotColdCircle;
    private TextView hotColdText, distanceText, countDownText;

    private CountDownTimer countDownTimer;
    private long timeLeftMilliseconds = 180000; // 3 min
    private boolean timerRunning;

    public static final int DEFAULT_UPDATE_INTERVAL = 3;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private static final int PERMISSIONS_WAKELOCK = 98;

    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private PowerManager.WakeLock wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunter_screen);

        hotColdCircle = findViewById(R.id.hotColdCircle);
        hotColdText = findViewById(R.id.hotCold);
        distanceText = findViewById(R.id.distance);

        Drawable hotColdCircleDrawable = hotColdCircle.getBackground();

        locationRequest = LocationRequest.create()
                .setInterval(1000 * DEFAULT_UPDATE_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        //event that is triggered whenever the time interval is met
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                double playerLongitude = locationResult.getLastLocation().getLongitude();
                double playerLatitude = locationResult.getLastLocation().getLatitude();
                double playerAltitude = locationResult.getLastLocation().getAltitude();

                System.out.println("Current :::::: " + playerLatitude + ", " + playerLongitude + ", " + playerAltitude);

                LocationDTO locationDTO = new LocationDTO(Player.getGlobalName(),
                        playerLatitude, playerLongitude, playerAltitude);

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
                            if(wl.isHeld()) {
                                wl.release();
                            }
                            finish();
                            startActivity(new Intent(HunterScreenActivity.this, HomeScreenActivity.class));
                        } else if (gameStateResult == 5) {
                            Toast.makeText(HunterScreenActivity.this, "You WON!", Toast.LENGTH_SHORT).show();
                            stopLocationUpdates();
                            if(wl.isHeld()) {
                                wl.release();
                            }
                            finish();
                            startActivity(new Intent(HunterScreenActivity.this, HomeScreenActivity.class));
                        }

                        Double distance = response.body().getDistance();
                        System.out.println("=======================> " + distance);

                        if (distance == -1) {
                            //waiting for he fox to send location
                            //or go to next call (denotes the last one)
                            return;
                        } else if (distance < 11) {
                            //hot
                            hotColdCircle.getBackground().setColorFilter(getColor(R.color.hot), PorterDuff.Mode.SRC_ATOP);
                            hotColdText.setText("HOT");
                        } else if (distance < 22) {
                            //warm
                            hotColdCircle.getBackground().setColorFilter(getColor(R.color.warm), PorterDuff.Mode.SRC_ATOP);
                            hotColdText.setText("WARM");
                        } else if (distance < 33) {
                            //cold
                            hotColdCircle.getBackground().setColorFilter(getColor(R.color.cold), PorterDuff.Mode.SRC_ATOP);
                            hotColdText.setText("COLD");
                        } else if (distance < 44) {
                            //verycold
                            hotColdCircle.getBackground().setColorFilter(getColor(R.color.very_cold), PorterDuff.Mode.SRC_ATOP);
                            hotColdText.setText("VERY COLD");
                        } else if (distance > 55) {
                            hotColdCircle.getBackground().setColorFilter(getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                            hotColdText.setText("OUT OF BOUNDS");
                        }
                    }

                    @Override
                    public void onFailure(Call<DistanceDTO> call, Throwable t) {
                        Toast.makeText(HunterScreenActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        acquireWakeLock();
        configureGPS();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
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
            case PERMISSIONS_WAKELOCK:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    Toast.makeText(this, "Wakelock permission must be granted in order to function!", Toast.LENGTH_SHORT).show();
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
                        updateUIValues(location);
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

    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            PowerManager pm = (PowerManager) HunterScreenActivity.this.getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
            wl.acquire();
        } else {
            //permissions not granted yet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WAKE_LOCK}, PERMISSIONS_WAKELOCK);
            }
        }
    }

    private void updateUIValues(Location lastLocation) {
        //primesc distanta si status game
    }

}