package com.example.foxontherun.server;

import com.example.foxontherun.model.LocationDTO;
import com.example.foxontherun.model.Player;
import com.example.foxontherun.model.Room;
import com.google.android.gms.location.LocationCallback;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GameService {

    @GET("game/rooms")
    Call<List<Room>> getRooms();

    @POST("game/create/{roomName}")
    Call<ResponseBody> createRoom(@Path("roomName") String roomName, @Body Player player);

    @POST("game/join/{roomName}")
    Call<ResponseBody> joinRoom(@Path("roomName") String roomName, @Body Player player);

    @POST("game/location/{roomName}")
    Call<ResponseBody> calculateDistance(@Path("roomName") String roomName, @Body LocationDTO playerLocation);

    @POST("game/assign/{roomName}")
    Call<ResponseBody> assignRoles(@Path("roomName") String roomName);

    @POST("game/exit/{roomName}/{playerName}")
    Call<ResponseBody> exitGameSession(@Path("roomName") String roomName, @Path("playerName") String playerName);

    @POST("game/end/{roomName}/{whoWon}")
    Call<ResponseBody> endGameRound(@Path("roomName") String roomName, @Path("whoWon") Boolean whoWon);

}
