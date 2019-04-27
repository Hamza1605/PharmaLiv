package com.example.pharmaliv;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class DeliveryManActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener stateListener;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_man);
        final Toolbar toolbar = findViewById(R.id.dltoolbar);
        setSupportActionBar(toolbar);
        final DatabaseReference deliveryReference = FirebaseDatabase.getInstance().getReference("Delivery Man");
        final Switch activeStatus = findViewById(R.id.active_status);
        activeStatus.setTextOn(getString(R.string.on_hold));
        activeStatus.setTextOff(getString(R.string.out_of_service));
        activeStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    deliveryReference.child("dl" + user.getUid()).child("Status").setValue("0");
                else
                    deliveryReference.child("dl" + user.getUid()).child("Status").setValue("2");
            }
        });
        final ArrayList<Mission> missions = new ArrayList<>();
        final MissionAdapter adapter = new MissionAdapter(DeliveryManActivity.this, missions);
        ListView listView = findViewById(R.id.dlmissions);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DeliveryManActivity.this, MissionFragment.class);
                intent.putExtra("ph", missions.get(position).pharmacyID);
                intent.putExtra("cl", missions.get(position).clientID);
                intent.putExtra("lat", missions.get(position).latitude);
                intent.putExtra("lng", missions.get(position).langitude);
                intent.putExtra("order", missions.get(position).orderID);
                startActivity(intent);
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Ordinance");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                    if ((Objects.equals(ds.child("Delivery Man").getValue(String.class), "dl" + user.getUid()))
                            && (ds.child("Delivery Man").exists())) {
                        missions.add(new Mission(ds.getKey(),
                                ds.child("Client").getValue(String.class),
                                ds.child("Pharmacy").getValue(String.class),
                                ds.child("Address").child("Latitude").getValue(String.class),
                                ds.child("Address").child("Longitude").getValue(String.class)));
                        adapter.notifyDataSetChanged();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        auth = FirebaseAuth.getInstance();
        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = auth.getCurrentUser();
                if (user != null) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Delivery Man");
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.child("dl" + user.getUid()).child("Family Name").getValue(String.class)
                                    + " " +
                                    dataSnapshot.child("dl" + user.getUid()).child("First Name").getValue(String.class);
                            toolbar.setTitle(name);
                            if (Objects.equals(dataSnapshot.child("dl" + user.getUid())
                                    .child("Status").getValue(String.class), "0")) {
                                activeStatus.setChecked(true);
                            } else if (Objects.equals(dataSnapshot.child("dl" + user.getUid())
                                    .child("Status").getValue(String.class), "1")) {
                                activeStatus.setEnabled(false);
                                activeStatus.setText(getString(R.string.occupied));
                            } else if (Objects.equals(dataSnapshot.child("dl" + user.getUid())
                                    .child("Status").getValue(String.class), "2")) {
                                activeStatus.setChecked(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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
}

class Mission {
    String orderID;
    String clientID;
    String pharmacyID;
    String latitude;
    String langitude;

    Mission(String orderID, String clientID, String pharmacyID, String latitude, String langitude) {
        this.orderID = orderID;
        this.clientID = clientID;
        this.pharmacyID = pharmacyID;
        this.latitude = latitude;
        this.langitude = langitude;
    }
}

class MissionAdapter extends ArrayAdapter<Mission> {

    public MissionAdapter(@NonNull Context context, @NonNull ArrayList<Mission> missions) {
        super(context, 0, missions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.order, parent, false);
        TextView ph_name = view.findViewById(R.id.m_ph_name);
        TextView cl_name = view.findViewById(R.id.m_cl_name);
        ph_name.setText(Objects.requireNonNull(getItem(position)).pharmacyID);
        cl_name.setText(Objects.requireNonNull(getItem(position)).clientID);
        return view;
    }
}