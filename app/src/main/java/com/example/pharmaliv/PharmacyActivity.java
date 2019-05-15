package com.example.pharmaliv;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class PharmacyActivity extends AppCompatActivity {

    FirebaseAuth.AuthStateListener stateListener;
    FirebaseAuth auth;
    FirebaseUser user;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy);

        final Toolbar toolbar = findViewById(R.id.ph_toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        bundle = new Bundle();

        Button clientsOrders = findViewById(R.id.cl_orders);
        Button delivery_men_list = findViewById(R.id.dl_list);
        Button addMedication = findViewById(R.id.add_med);

        clientsOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PharmacyActivity.this, ClientsRequestsActivity.class));
            }
        });

        delivery_men_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PharmacyActivity.this, DeliveryMenActivity.class));
            }
        });

        addMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(PharmacyActivity.this);
                AlertDialog dialog = new AlertDialog.Builder(PharmacyActivity.this)
                        .setTitle(getString(R.string.add_medication))
                        .setView(editText)
                        .setPositiveButton(getString(R.string.add_medication), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseReference medRef = FirebaseDatabase.getInstance().getReference().child("Medication");
                                if (!TextUtils.isEmpty(editText.getText().toString())) {
                                    medRef.push().child("Name").setValue(editText.getText().toString());
                                } else {
                                    editText.setError("");
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                dialog.show();
            }
        });

        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = auth.getCurrentUser();
                if (user != null) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Pharmacy");
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.child("ph" + user.getUid()).child("Name").getValue(String.class);
                            //    bundle.putDouble("Latitude", Double.parseDouble(Objects.requireNonNull(dataSnapshot.child("Latitude").getValue(String.class))));
                            // bundle.putDouble("Longitude", Double.parseDouble(Objects.requireNonNull(dataSnapshot.child("Longitude").getValue(String.class))));
                            toolbar.setTitle(name);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    startActivity(new Intent(getApplicationContext(), SignINActivity.class));
                    finish();
                }
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(stateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(stateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sing_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sing_in) {
            auth.signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
