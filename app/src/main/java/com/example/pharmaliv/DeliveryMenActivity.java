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
                    DeliveryMan deliveryMan = ds.getValue(DeliveryMan.class);
                    if (Objects.requireNonNull(deliveryMan).getState().equals("0")) {
                        deliveryMen.add(deliveryMan);
                        adapter.notifyDataSetChanged();
                    }
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
                    intent.putExtra("Delivery Man", "dl" + deliveryMen.get(position).getLogin_ID());
                    DeliveryMenActivity.this.setResult(RESULT_OK, intent);
                }
            }
        });
    }





    class DeliveryManAdapter extends ArrayAdapter<DeliveryMan> {

        private Context context;

        DeliveryManAdapter(@NonNull Context context, ArrayList<DeliveryMan> deliveryMen) {
            super(context, 0, deliveryMen);
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
                TextView dlName = view.findViewById(android.R.id.text1);
                TextView dlLocation = view.findViewById(android.R.id.text2);
                String s = Objects.requireNonNull(getItem(position)).getFamily_Name() + " " +
                        Objects.requireNonNull(getItem(position)).getFirst_Name();
                dlName.setText(s);
                dlLocation.setText("");
            }
            return view;
        }
    }
}
