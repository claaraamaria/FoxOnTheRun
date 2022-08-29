package com.example.foxontherun.server;

import com.example.foxontherun.model.DistanceDTO;
import com.example.foxontherun.model.LocationDTO;
import com.example.foxontherun.model.Player;
import com.example.foxontherun.model.Room;
import com.google.android.gms.location.LocationCallback;

import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GameService {

    @POST("game/join/{roomName}/{playerName}")
    Call<Boolean> joinRoom(@Path("roomName") String roomName, @Path("playerName") String playerName);

    @POST("game/location/{roomName}")
    Call<DistanceDTO> updateLocation(@Path("roomName") String roomName, @Body LocationDTO playerLocation);

    @GET("game/role/{roomName}/{playerName}")
    Call<Boolean> getRole(@Path("roomName") String roomName, @Path("playerName") String playerName);

    @GET("game/time/{roomName}")
    Call<Date> getStartDate(@Path("roomName") String roomName);

    @GET("game/configuration")
    Call<Map<String, Long>> getConfiguration();
}
