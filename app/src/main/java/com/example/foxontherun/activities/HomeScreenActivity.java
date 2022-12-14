package com.example.foxontherun.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foxontherun.R;
import com.example.foxontherun.helper.Helper;
import com.example.foxontherun.model.GameConfiguration;
import com.example.foxontherun.model.Player;
import com.example.foxontherun.model.User;
import com.example.foxontherun.server.GameService;
import com.example.foxontherun.server.RESTClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    private TextView usernameTextView;
    private Button historyBtn, profileBtn, playBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        initializeGameConfiguration();

        usernameTextView = findViewById(R.id.username);

        historyBtn = findViewById(R.id.gameHistory);
        historyBtn.setOnClickListener(this);

        profileBtn = findViewById(R.id.profile);
        profileBtn.setOnClickListener(this);

        playBtn = findViewById(R.id.play);
        playBtn.setOnClickListener(this);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Player.getGlobalRoomName() == null) {
                    showCodeRoomDialog();
                } else {
                    roomCodeCorrect(Player.getGlobalRoomName());
                }
            }
        });

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if (userProfile != null) {
                    Player.setGlobalName(userProfile.username);
                    String username = userProfile.username;
                    usernameTextView.setText(username + " !");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeScreenActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Player.getGlobalRoomName() == null) {
            playBtn.setText("LET'S PLAY");
        } else {
            playBtn.setText("RETURN TO ROOM");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gameHistory:
                startActivity(new Intent(this, GameHistoryActivity.class));
                break;
            case R.id.profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
        }
    }

    public void initializeGameConfiguration() {
        Call<Map<String, Long>> initializeConfigCallResult = RESTClient
                .getInstance()
                .getApi()
                .getConfiguration();

        initializeConfigCallResult.enqueue(new Callback<Map<String, Long>>() {
            @Override
            public void onResponse(Call<Map<String, Long>> call, Response<Map<String, Long>> response) {
                Map<String, Long> responseBody = response.body();
                GameConfiguration.setWaitLobbyTimer(responseBody.get("waitLobbyTimer"));
                GameConfiguration.setHideTimer(responseBody.get("hideTimer"));
                GameConfiguration.setGameOnTimer(responseBody.get("gameOnTimer"));
            }

            @Override
            public void onFailure(Call<Map<String, Long>> call, Throwable t) {
                boolean executed = call.isExecuted();
                String message = t.getMessage();
                //do nothing
            }
        });
    }

    private void showCodeRoomDialog() {
        final Dialog dialog = new Dialog(HomeScreenActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.code_room_dialog);

        final EditText roomCodeEt = dialog.findViewById(R.id.editTextCode);
        Button submitButton = dialog.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (roomCodeEt.getText().toString().isEmpty()) {
                    roomCodeEt.setError("Name should not be empty!");
                    roomCodeEt.requestFocus();
                } else {
                    String roomCode = roomCodeEt.getText().toString();

                    Player.setGlobalRoomName(roomCode);

                    roomCodeCorrect(roomCode);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void roomCodeCorrect(String roomCode) {
        Call<Integer> callResult = RESTClient
                .getInstance()
                .getApi()
                .joinRoom(roomCode, Player.getGlobalName());

        callResult.enqueue(new Callback<Integer>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == null) {
                    return;
                }

                Integer result = response.body();

                if (result == null) {
                    return;
                }

                Helper.getRoleCall(HomeScreenActivity.this, result);
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Player.setGlobalRoomName(null);
                Toast.makeText(HomeScreenActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}