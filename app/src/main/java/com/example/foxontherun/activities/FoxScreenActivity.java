package com.example.foxontherun.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Toast;

import com.example.foxontherun.R;
import com.example.foxontherun.model.DistanceDTO;
import com.example.foxontherun.model.LocationDTO;
import com.example.foxontherun.model.Player;
import com.example.foxontherun.server.RESTClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoxScreenActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int DEFAULT_UPDATE_INTERVAL = 3;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private static final int PERMISSIONS_WAKELOCK = 98;

    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private PowerManager.WakeLock wl;

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fox_screen);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        locationRequest = LocationRequest.create()
                .setInterval(1000 * DEFAULT_UPDATE_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        //event that is triggered whenever the time interval is met
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if(locationResult.getLastLocation() == null) {
                    return;
                }

                placeMarker(locationResult.getLastLocation());

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
                            Toast.makeText(FoxScreenActivity.this, "You WON!", Toast.LENGTH_SHORT).show();
                            stopLocationUpdates();
                            if(wl.isHeld()) {
                                wl.release();
                            }
                            finish();
                            startActivity(new Intent(FoxScreenActivity.this, HomeScreenActivity.class));
                        } else if (gameStateResult == 5) {
                            Toast.makeText(FoxScreenActivity.this, "Hunters WON!", Toast.LENGTH_SHORT).show();
                            stopLocationUpdates();
                            if(wl.isHeld()) {
                                wl.release();
                            }
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
            PowerManager pm = (PowerManager) FoxScreenActivity.this.getSystemService(Context.POWER_SERVICE);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create()
                .setInterval(1000 * DEFAULT_UPDATE_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void placeMarker(@NonNull Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap=googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                configureGPS();
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
}