package com.example.pharmaliv;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DeliveryMenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_men);
        final ArrayList<DeliveryMan> deliveryMen = new ArrayList<>();
        final DeliveryManAdapter adapter = new DeliveryManAdapter(Objects.requireNonNull(getApplicationContext()), deliveryMen);
        ListView listView = findViewById(R.id.dl_list);
        listView.setAdapter(adapter);
        DatabaseReference referenceDelivery = FirebaseDatabase.getInstance().getReference().child("Delivery Man");

        referenceDelivery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    deliveryMen.add(new DeliveryMan("dl" + ds.child("Login ID").getValue(String.class),
                            ds.child("Family Name").getValue(String.class) + " " +
                                    ds.child("First Name").getValue(String.class)));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getIntent().getIntExtra("send", 0) == 1) {
                    Intent intent = DeliveryMenActivity.this.getIntent();
                    intent.putExtra("Delivery Man", deliveryMen.get(position).dlID);
                    DeliveryMenActivity.this.setResult(RESULT_OK, intent);
                }
            }
        });
    }


    class DeliveryMan {
        String dlID;
        String dlName;
        Double Latitude;
        Double Longitude;

        DeliveryMan(String dlID, String dlName, Double Latitude, Double Longitude) {
            this.dlID = dlID;
            this.dlName = dlName;
            this.Latitude = Latitude;
            this.Longitude = Longitude;
        }

        DeliveryMan(String dlID, String dlName) {
            this.dlID = dlID;
            this.dlName = dlName;
            this.Latitude = 0.0;
            this.Longitude = 0.0;
        }
    }

    class DeliveryManAdapter extends ArrayAdapter<DeliveryMan> {

        private Context context;
        private Double Latitude;
        private Double Longitude;


        DeliveryManAdapter(@NonNull Context context, ArrayList<DeliveryMan> deliveryMen) {
            super(context, 0, deliveryMen);
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = null;
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
                TextView dlName = view.findViewById(android.R.id.text1);
                TextView dlLocation = view.findViewById(android.R.id.text2);
                dlName.setText(Objects.requireNonNull(getItem(position)).dlName);
                float[] v = new float[1];
                Location.distanceBetween(Latitude, Longitude,
                        Objects.requireNonNull(getItem(position)).Latitude,
                        Objects.requireNonNull(getItem(position)).Longitude, v);
                try {
                    addresses = geocoder.getFromLocation(
                            Objects.requireNonNull(getItem(position)).Latitude,
                            Objects.requireNonNull(getItem(position)).Longitude, 1);
                } catch (IOException e1) {
                    dlLocation.setText(dlLocation.getText().toString() + R.string.service_not_available);
                } catch (IllegalArgumentException e2) {
                    dlLocation.setText(dlLocation.getText().toString() + R.string.invalid_information);
                }
                if ((addresses != null ? addresses.size() : 0) != 0) {
                    Address address = addresses.get(0);
                    dlLocation.setText(address.getAddressLine(0));
                } else
                    dlLocation.setText(dlLocation.getText().toString() + R.string.address_not_found);
                dlLocation.setText(Arrays.toString(v));
            }
            return view;
        }
    }
}
