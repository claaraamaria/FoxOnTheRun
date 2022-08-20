package com.example.foxontherun.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foxontherun.R;
import com.example.foxontherun.model.Room;
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

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;

    private TextView usernameTextView;
    private Button historyBtn, profileBtn, playBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up_screen);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();

        usernameTextView = findViewById(R.id.username);

        historyBtn = findViewById(R.id.gameHistory);
        historyBtn.setOnClickListener(this);

        profileBtn = findViewById(R.id.profile);
        profileBtn.setOnClickListener(this);

        playBtn = findViewById(R.id.play);
        playBtn.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        // Exemplu de call catre API
        Call<List<Room>> callResult = RESTClient
                .getInstance()
                .getApi()
                .getRooms();

        callResult.enqueue(new Callback<List<Room>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                List<Room> roomsList = response.body();
                if(roomsList != null){
                    roomsList.stream().forEach(r -> {
                        System.out.println(r.getRoomName());
                    });
                }
                else{
                    System.out.println(response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);

                if (userProfile != null) {
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gameHistory:
                startActivity(new Intent(this, GameHistoryActivity.class));
                break;
            case R.id.profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.play:
                startActivity(new Intent(this, GPSActivity.class));
                break;
        }
    }
}