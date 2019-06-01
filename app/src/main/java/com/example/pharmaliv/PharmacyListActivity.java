package com.example.pharmaliv;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import mumayank.com.airlocationlibrary.AirLocation;

public class PharmacyListActivity extends AppCompatActivity {

    private ArrayList<Pharmacy> pharmacyList;
    private PharmacyAdapter adapter;
    private Location clientLocation;
    private AirLocation airLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_list);

        airLocation = new AirLocation(this, true,
                true, new AirLocation.Callbacks() {
            @Override
            public void onSuccess(@NonNull Location location) {
                clientLocation = location;
            }

            @Override
            public void onFailed(@NonNull AirLocation.LocationFailedEnum locationFailedEnum) {

            }
        });

        pharmacyList = new ArrayList<>();
        adapter = new PharmacyAdapter(this, pharmacyList, clientLocation);
        ListView listViewPharmacy = findViewById(R.id.pharmacies_list);
        listViewPharmacy.setAdapter(adapter);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Pharmacy");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    pharmacyList.add(ds.getValue(Pharmacy.class));
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
                                intent.putExtra("Ph_ID", "ph" + pharmacyList.get(position).getLoginID());
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        airLocation.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        airLocation.onActivityResult(requestCode, resultCode, data);
    }

    class PharmacyAdapter extends ArrayAdapter<Pharmacy> {


        PharmacyAdapter(@NonNull Context context, ArrayList<Pharmacy> pharmacies, Location location) {
            super(context, 0, pharmacies);
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
            if (clientLocation != null) {
                Location.distanceBetween(clientLocation.getLatitude(), clientLocation.getLongitude(),
                        Objects.requireNonNull(getItem(position)).getLatitude(),
                        Objects.requireNonNull(getItem(position)).getLongitude(), v);
            }

            pharmacy_name.setText(Objects.requireNonNull(getItem(position)).getName());
            pharmacy_distance.setText(String.valueOf(v[0]));
            return view;
        }
    }
}

