package com.example.pharmaliv;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PharmacyListActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_LOCATION = 2;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location myLocation;
    private ArrayList<Pharmacy> pharmacyList;
    private PharmacyAdapter adapter;
    private ListView listViewPharmacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_list);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        pharmacyList = new ArrayList<>();
        adapter = new PharmacyAdapter(this, pharmacyList, myLocation);
        listViewPharmacy = findViewById(R.id.pharmacies_list);
        listViewPharmacy.setAdapter(adapter);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Pharmacy");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    pharmacyList.add(new Pharmacy(
                            ds.child("Login ID").getValue(String.class),
                            ds.child("Name").getValue(String.class),
                            Double.parseDouble(Objects.requireNonNull(ds.child("Latitude").getValue(String.class))),
                            Double.parseDouble(Objects.requireNonNull(ds.child("Longitude").getValue(String.class)))
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
    protected void onStart() {
        super.onStart();
        googleApiClient.reconnect();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        setLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == REQUEST_LOCATION) && (grantResults.length == 2) &&
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            setLocation();
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    setLocation(location);
                }
            });
        }
    }

    public void setLocation(Location myLocation) {
        this.myLocation = myLocation;
    }
}

class Pharmacy {

    String ph_id;
    String Name;
    Double Latitude;
    Double Longitude;

    Pharmacy(String ph_id, String Name, Double Latitude, Double Longitude) {
        this.ph_id = ph_id;
        this.Name = Name;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
    }
}

class PharmacyAdapter extends ArrayAdapter<Pharmacy> {

    private Context context;
    private Location location;

    PharmacyAdapter(@NonNull Context context, ArrayList<Pharmacy> pharmacies, Location location) {
        super(context, 0, pharmacies);
        this.context = context;
        this.location = location;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.pharmacy_row, parent, false);
        TextView pharmacy_name = view.findViewById(R.id.ph_name);
        TextView pharmacy_address = view.findViewById(R.id.address);
        TextView pharmacy_distance = view.findViewById(R.id.distance);
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    Objects.requireNonNull(getItem(position)).Latitude,
                    Objects.requireNonNull(getItem(position)).Longitude, 1);
        } catch (IOException e1) {
            e1.printStackTrace();
            pharmacy_address.setText(R.string.service_not_available);
        } catch (
                IllegalArgumentException e2) {
            pharmacy_address.setText(R.string.invalid_information);
        }
        pharmacy_name.setText(Objects.requireNonNull(getItem(position)).Name);
        if ((addresses != null ? addresses.size() : 0) != 0) {
            Address address = addresses.get(0);
            pharmacy_address.setText(address.getAddressLine(0));
        } else {
            pharmacy_address.setText(R.string.address_not_found);
        }
        float[] v = new float[1];

        Location.distanceBetween(location.getLatitude(), location.getLongitude(), Objects.requireNonNull(getItem(position)).Latitude, Objects.requireNonNull(getItem(position)).Longitude, v);
        pharmacy_distance.setText(String.valueOf(v[0]) + " m");
        return view;
    }
}