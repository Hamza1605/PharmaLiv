package com.example.pharmaliv;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class OrdersActivity extends AppCompatActivity {

    private String s;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        final String Uid = getIntent().getStringExtra("Uid");
        final ArrayList<Order> orders = new ArrayList<>();
        final OrdersAdapter adapter = new OrdersAdapter(this, orders);
        reference = FirebaseDatabase.getInstance().getReference().child("Ordinance");
        final DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Pharmacy");
        ListView listView = findViewById(R.id.orders_list);
        listView.setAdapter(adapter);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    final String[] s = new String[1];
                    if ((Objects.equals(ds.child("Client").getValue(String.class), Uid))
                            && (!Objects.equals(ds.child("State").getValue(String.class), "3"))) {
                        reference1.child(Objects.requireNonNull(ds.child("Pharmacy").getValue(String.class)))
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        s[0] = dataSnapshot.child("Name").getValue(String.class);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                        Toast.makeText(OrdersActivity.this, s[0], Toast.LENGTH_SHORT).show();
                        orders.add(new Order(
                                ds.getKey(),
                                s[0],
                                ds.child("Date").getValue(String.class),
                                ds.child("Time").getValue(String.class),
                                ds.child("State").getValue(String.class),
                                ds.child("Note").getValue(String.class)));
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
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (orders.get(position).state == "1") {
                    AlertDialog.Builder builder = new AlertDialog.Builder(OrdersActivity.this)
                            .setTitle("")
                            .setPositiveButton("Set Address", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    s = orders.get(position).ordinance;
                                    startActivityForResult(new Intent(OrdersActivity.this,
                                            MapsActivity.class), 2);
                                }
                            })
                            .setNegativeButton(getString(R.string.decline),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                    builder.show();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            reference.child(s).child("Address").child("Latitude")
                    .setValue(Objects.requireNonNull(data).getDoubleExtra("latitude", 0));
            reference.child(s).child("Address").child("Longitude")
                    .setValue(Objects.requireNonNull(data).getDoubleExtra("longitude", 0));
            reference.child(s).child("State").setValue("3");
        }
    }
}

class Order {

    String ordinance;
    String ph_Name;
    String date;
    String time;
    String state;
    String note;

    Order(String ordinance, String ph_Name, String date, String time, String state, String note) {
        this.ordinance = ordinance;
        this.ph_Name = ph_Name;
        this.date = date;
        this.time = time;
        this.state = state;
        this.note = note;
    }
}

class OrdersAdapter extends ArrayAdapter<Order> {

    OrdersAdapter(@NonNull Context context, ArrayList<Order> orders) {
        super(context, 0, orders);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.order_row, parent, false);
        TextView ph_Name = view.findViewById(R.id.ord_ph_name);
        TextView date = view.findViewById(R.id.ord_date);
        TextView time = view.findViewById(R.id.ord_time);
        TextView state = view.findViewById(R.id.ord_state);
        TextView note = view.findViewById(R.id.ord_note);
        ph_Name.setText(Objects.requireNonNull(getItem(position)).ph_Name);
        date.setText(Objects.requireNonNull(getItem(position)).date);
        time.setText(Objects.requireNonNull(getItem(position)).time);
        note.setText(Objects.requireNonNull(getItem(position)).note);
        switch (Objects.requireNonNull(getItem(position)).state) {
            case "0":
                state.setText(R.string.waiting_reply);
                break;
            case "1":
                state.setText(R.string.accepted);
                break;
            case "2":
                state.setText((R.string.declined));
                break;
        }

        return view;
    }
}
