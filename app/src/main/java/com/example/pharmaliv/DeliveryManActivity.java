package com.example.pharmaliv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
                    deliveryReference.child("dl" + user.getUid()).child("state").setValue("0");
                else
                    deliveryReference.child("dl" + user.getUid()).child("state").setValue("2");
            }
        });

        final ArrayList<Prescription> prescriptions = new ArrayList<>();
        final MissionAdapter adapter = new MissionAdapter(DeliveryManActivity.this, prescriptions);
        ListView listView = findViewById(R.id.dlmissions);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DeliveryManActivity.this, MissionActivity.class);
                intent.putExtra("pr", prescriptions.get(position).getId());
                intent.putExtra("ph", prescriptions.get(position).getPharmacy_ID());
                intent.putExtra("cl", prescriptions.get(position).getClient_ID());
                intent.putExtra("st", prescriptions.get(position).getState());
                intent.putExtra("lat", Double.parseDouble(prescriptions.get(position).getLatitude()));
                intent.putExtra("lng", Double.parseDouble(prescriptions.get(position).getLongitude()));
                startActivity(intent);
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Prescription");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("delivery_ID").exists()) {
                        Prescription prescription = ds.getValue(Prescription.class);
                        if (Objects.requireNonNull(prescription).getDelivery_ID().equals("dl" + user.getUid())) {
                            if (prescription.getState().equals("5") || prescription.getState().equals("7")) {
                                if (contains(prescription.getId(), prescriptions) != prescriptions.size()) {
                                    prescriptions.set(contains(prescription.getId(), prescriptions), prescription);
                                } else {
                                    prescriptions.add(prescription);
                                }
                                adapter.notifyDataSetChanged();
                            } else if (contains(prescription.getId(), prescriptions) != prescriptions.size()) {
                                prescriptions.remove(contains(prescription.getId(), prescriptions));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
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
                            DeliveryMan deliveryMan = dataSnapshot.child("dl" + user.getUid())
                                    .getValue(DeliveryMan.class);
                            String name = Objects.requireNonNull(deliveryMan).getFamily_Name()
                                    + " " + deliveryMan.getFirst_Name();
                            toolbar.setTitle(name);
                            switch (deliveryMan.getState()) {
                                case "0":
                                    activeStatus.setChecked(true);
                                    break;
                                case "1":
                                    activeStatus.setEnabled(false);
                                    activeStatus.setText(getString(R.string.occupied));
                                    break;
                                case "2":
                                    activeStatus.setChecked(false);
                                    break;
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
    }

    public int contains(String s, ArrayList<Prescription> prescriptions) {
        int b = prescriptions.size();
        for (int i = 0; i < prescriptions.size(); i++) {
            if (prescriptions.get(i).getId().equals(s)) {
                b = i;
                break;
            } else {
                i++;
            }
        }
        return b;
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

    class MissionAdapter extends ArrayAdapter<Prescription> {

        MissionAdapter(@NonNull Context context, @NonNull ArrayList<Prescription> prescriptions) {
            super(context, 0, prescriptions);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.order, parent, false);
            final TextView ph_name = view.findViewById(R.id.m_ph_name);
            final TextView cl_name = view.findViewById(R.id.m_cl_name);
            TextView datetime = view.findViewById(R.id.m_dl_dt);

            String s = Objects.requireNonNull(getItem(position)).getDelivery_Date()
                    + " " + Objects.requireNonNull(getItem(position)).getDelivery_Time();
            datetime.setText(s);
            DatabaseReference clientReference = FirebaseDatabase.getInstance().getReference("Client")
                    .child(Objects.requireNonNull(getItem(position)).getClient_ID());
            clientReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String s = dataSnapshot.child("family_Name").getValue(String.class) + " " +
                            dataSnapshot.child("first_Name").getValue(String.class);
                    cl_name.setText(s);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            DatabaseReference pharmacyReference = FirebaseDatabase.getInstance().getReference("Pharmacy")
                    .child(Objects.requireNonNull(getItem(position)).getPharmacy_ID());
            pharmacyReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String s = dataSnapshot.child("name").getValue(String.class);
                    ph_name.setText(s);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return view;
        }
    }
}
