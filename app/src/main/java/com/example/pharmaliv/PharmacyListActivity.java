package com.example.pharmaliv;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class PharmacyListActivity extends AppCompatActivity {

    private ArrayList<Pharmacy> pharmacyList;
    private PharmacyAdapter adapter;
    private Location clientLocation = null;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_list);

        if (!runtimePermissions())
            startService(new Intent(PharmacyListActivity.this, GPSService.class));


        pharmacyList = new ArrayList<>();
        adapter = new PharmacyAdapter(this, pharmacyList, clientLocation);
        ListView listViewPharmacy = findViewById(R.id.pharmacies_list);
        listViewPharmacy.setAdapter(adapter);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Pharmacy");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    pharmacyList.add(new Pharmacy(
                            ds.child("Login ID").getValue(String.class),
                            ds.child("Name").getValue(String.class),
                            ds.child("Latitude").getValue(String.class),
                            ds.child("Longitude").getValue(String.class)
                    ));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        listViewPharmacy.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PharmacyListActivity.this)
                        .setTitle(R.string.confirm)
                        .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = PharmacyListActivity.this.getIntent();
                                intent.putExtra("Ph_ID", "ph" + pharmacyList.get(position).ph_id);
                                PharmacyListActivity.this.setResult(RESULT_OK, intent);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!pharmacyList.isEmpty()) {
            if (broadcastReceiver == null) {
                broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        clientLocation.setLatitude(intent.getDoubleExtra("lat", 0));
                        clientLocation.setLongitude(intent.getDoubleExtra("lng", 0));
                        adapter.notifyDataSetChanged();
                    }
                };
            }
            registerReceiver(broadcastReceiver, new IntentFilter("location updates"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(PharmacyListActivity.this, GPSService.class));
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
    }

    private boolean runtimePermissions() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(PharmacyListActivity.this, GPSService.class));
            } else {
                runtimePermissions();
            }
        }
    }

    class Pharmacy {

        String ph_id;
        String Name;
        Double Latitude;
        Double Longitude;

        Pharmacy(String ph_id, String Name, String Latitude, String Longitude) {
            this.ph_id = ph_id;
            this.Name = Name;
            this.Latitude = Double.parseDouble(Latitude);
            this.Longitude = Double.parseDouble(Longitude);
        }
    }

    class PharmacyAdapter extends ArrayAdapter<Pharmacy> {


        private Location location;

        PharmacyAdapter(@NonNull Context context, ArrayList<Pharmacy> pharmacies, Location location) {
            super(context, 0, pharmacies);
            this.location = location;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.pharmacy_row, parent, false);

            TextView pharmacy_name = view.findViewById(R.id.ph_name);
            TextView pharmacy_distance = view.findViewById(R.id.phrow_distance);

            float[] v = new float[1];
            if (location != null) {
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        Objects.requireNonNull(getItem(position)).Latitude,
                        Objects.requireNonNull(getItem(position)).Longitude, v);
            }

            pharmacy_name.setText(Objects.requireNonNull(getItem(position)).Name);
            pharmacy_distance.setText(String.valueOf(v[0]));
            return view;
        }
    }
}

