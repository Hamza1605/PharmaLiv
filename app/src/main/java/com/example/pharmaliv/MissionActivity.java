package com.example.pharmaliv;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mumayank.com.airlocationlibrary.AirLocation;

public class MissionActivity extends AppCompatActivity {

    private Location l;
    private AirLocation airLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);
        final TextView phName = findViewById(R.id.mf_ph_name);
        final TextView clName = findViewById(R.id.mf_cl_name);
        final TextView date = findViewById(R.id.mf_dt);
        final TextView time = findViewById(R.id.mf_tm);
        final TextView note = findViewById(R.id.mf_note);
        final TextView textView = findViewById(R.id.textView3);
        final Button acc = findViewById(R.id.mf_acc);
        final Button dec = findViewById(R.id.mf_dec);
        Button add = findViewById(R.id.mf_add);
        Button call = findViewById(R.id.mf_call);

        final DatabaseReference prescriptionReference = FirebaseDatabase.getInstance().getReference("Prescription")
                .child(getIntent().getStringExtra("pr"));
        prescriptionReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                date.setText(dataSnapshot.child("delivery_Date").getValue(String.class));
                time.setText(dataSnapshot.child("delivery_Time").getValue(String.class));
                note.setText(dataSnapshot.child("deliveryMan_Note").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        DatabaseReference clientReference = FirebaseDatabase.getInstance().getReference("Client");
        clientReference.child(getIntent().getStringExtra("cl"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String s = dataSnapshot.child("family_Name").getValue(String.class) + " " +
                                dataSnapshot.child("first_Name").getValue(String.class);
                        clName.setText(s);
                        textView.setText(dataSnapshot.child("phone").getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        DatabaseReference pharmacyReference = FirebaseDatabase.getInstance().getReference("Pharmacy");
        pharmacyReference.child(getIntent().getStringExtra("ph"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String s = dataSnapshot.child("name").getValue(String.class);
                        phName.setText(s);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (getIntent().getStringExtra("st").equals("7")) {
            acc.setText(getString(R.string.delivered));
            dec.setText(getString(R.string.not_delivered));
        }

        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getStringExtra("st").equals(("5")))
                    prescriptionReference.child("state").setValue("7");
                else if (getIntent().getStringExtra("st").equals(("7")))
                    prescriptionReference.child("state").setValue("9");
                dec.setEnabled(false);
                acc.setEnabled(false);
            }
        });

        dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getStringExtra("st").equals(("5")))
                    prescriptionReference.child("state").setValue("5");
                else if (getIntent().getStringExtra("st").equals(("7")))
                    prescriptionReference.child("state").setValue("10");
                dec.setEnabled(false);
                acc.setEnabled(false);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                airLocation = new AirLocation(MissionActivity.this,
                        true, true,
                        new AirLocation.Callbacks() {
                            @Override
                            public void onSuccess(@NonNull Location location) {
                                l = location;
                            }

                            @Override
                            public void onFailed(@NonNull AirLocation.LocationFailedEnum locationFailedEnum) {

                            }
                        });
                Intent intent = new Intent(MissionActivity.this, MapsActivity.class);
                intent.putExtra("send", "0");
                intent.putExtra("mlat", getIntent().getDoubleExtra("lat", 0));
                intent.putExtra("mlng", getIntent().getDoubleExtra("lng", 0));
                if (l != null) {
                    intent.putExtra("llat", l.getLatitude());
                    intent.putExtra("llng", l.getLongitude());
                }
                startActivity(intent);
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setData(Uri.parse("tel:" + textView.getText().toString()));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        airLocation.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        airLocation.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
