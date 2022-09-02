package com.example.foxontherun.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.foxontherun.activities.FoxScreenActivity;
import com.example.foxontherun.activities.HideCounterActivity;
import com.example.foxontherun.activities.HunterScreenActivity;
import com.example.foxontherun.activities.WaitLobbyActivity;
import com.example.foxontherun.model.Player;
import com.example.foxontherun.server.RESTClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Helper extends Activity {

    public static void getRoleCall(Context context, Integer currentGameState) {
        Call<Boolean> roleAssignCall = RESTClient
                .getInstance()
                .getApi()
                .getRole(Player.getGlobalRoomName(), Player.getGlobalName());

        roleAssignCall.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(currentGameState == 0) {
                    context.startActivity(new Intent(context, WaitLobbyActivity.class));

                } else if (response.body() != null) {
                    Player.setGlobalRole(response.body());
                    if(currentGameState == 1) {
                        context.startActivity(new Intent(context, HideCounterActivity.class));

                    }else if(currentGameState == 2) {
                        if (Player.getGlobalRole() == true) {
                            context.startActivity(new Intent(context, HunterScreenActivity.class));

                        }  else if (Player.getGlobalRole() == false) {
                            context.startActivity(new Intent(context, FoxScreenActivity.class));

                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                //error
            }
        });
    }
}
