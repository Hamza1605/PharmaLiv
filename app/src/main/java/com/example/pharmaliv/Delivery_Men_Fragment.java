package com.example.pharmaliv;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class Delivery_Men_Fragment extends Fragment {


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery__men_, container, false);
        Bundle bundle = getArguments();
        final ArrayList<DeliveryMan> deliveryMen = new ArrayList<>();
        DeliveryManAdapter adapter = new DeliveryManAdapter(Objects.requireNonNull(getContext()), deliveryMen,
                Objects.requireNonNull(bundle).getDouble("Latitude"),
                Objects.requireNonNull(bundle).getDouble("Longitude"));
        ListView listView = view.findViewById(R.id.dl_list);
        listView.setAdapter(adapter);
        DatabaseReference referenceDelivery = FirebaseDatabase.getInstance().getReference().child("Delivery Man");
        DatabaseReference referenceLocation = FirebaseDatabase.getInstance().getReference().child("Location History");

        referenceDelivery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    deliveryMen.add(new DeliveryMan("dl" + ds.child("Login ID").getValue(String.class),
                            ds.child("Family Name").getValue(String.class) + " " + ds.child("First Name").getValue(String.class)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
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


    DeliveryManAdapter(@NonNull Context context, ArrayList<DeliveryMan> deliveryMen, Double Latitude, Double Longitude) {
        super(context, 0, deliveryMen);
        this.context = context;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        View view = convertView;
        if (view != null) {
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
