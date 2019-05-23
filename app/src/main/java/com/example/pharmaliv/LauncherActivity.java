package com.example.pharmaliv;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LauncherActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener stateListener;
    private FirebaseUser user;
    private ImageView logo, background;
    private TextView msg, noInternet;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        logo = findViewById(R.id.logo);
        background = findViewById(R.id.background);
        msg = findViewById(R.id.msg);
        noInternet = findViewById(R.id.desc);
        progressBar = findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkConnectivity();
            }
        }, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        auth.removeAuthStateListener(stateListener);
    }

    public void checkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        progressBar.setVisibility(View.INVISIBLE);
        if (networkInfo == null) {
            display();
        } else {
            checkAuth();
        }
    }

    public void checkAuth() {
        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = auth.getCurrentUser();
                if (user != null) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("Client").hasChild("cl" + user.getUid())) {
                                startActivity(new Intent(LauncherActivity.this, ClientActivity.class));
                            } else if (dataSnapshot.child("Pharmacy").hasChild("ph" + user.getUid())) {
                                startActivity(new Intent(LauncherActivity.this, PharmacyActivity.class));
                            } else if (dataSnapshot.child("Delivery Man").hasChild("dl" + user.getUid())) {
                                startActivity(new Intent(LauncherActivity.this, DeliveryManActivity.class));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    startActivity(new Intent(getApplicationContext(), SignINActivity.class));
                }
            }
        };
        auth.addAuthStateListener(stateListener);
    }

    public void display() {
        background.setVisibility(View.VISIBLE);
        logo.setVisibility(View.GONE);
        msg.setText(getString(R.string.oh_shucks));
        noInternet.setText(getString(R.string.no_internet));
    }
}
