package com.example.pharmaliv;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;


public class OrdersActivity extends AppCompatActivity {

    private String s;
    private DatabaseReference reference;
    private ArrayList<Order> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        final String Uid = getIntent().getStringExtra("Uid");
        orders = new ArrayList<>();
        final OrdersAdapter adapter = new OrdersAdapter(this, orders);
        reference = FirebaseDatabase.getInstance().getReference().child("Ordinance");
        ListView listView = findViewById(R.id.orders_list);
        listView.setAdapter(adapter);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if ((Objects.equals(ds.child("Client").getValue(String.class), Uid))
                            && (!Objects.equals(ds.child("State").getValue(String.class), "3"))) {
                        if (contains(ds.getKey(), orders) != orders.size()) {
                            orders.set(contains(ds.getKey(), orders),
                                    new Order(ds.getKey(),
                                            ds.child("Pharmacy").getValue(String.class),
                                            ds.child("Date").getValue(String.class),
                                            ds.child("Time").getValue(String.class),
                                            ds.child("State").getValue(String.class),
                                            ds.child("Note").getValue(String.class),
                                            ds.child("Total").getValue(String.class)));
                        } else {
                            orders.add(new Order(ds.getKey(),
                                    ds.child("Pharmacy").getValue(String.class),
                                    ds.child("Date").getValue(String.class),
                                    ds.child("Time").getValue(String.class),
                                    ds.child("State").getValue(String.class),
                                    ds.child("Note").getValue(String.class),
                                    ds.child("Total").getValue(String.class)));
                        }
                        adapter.notifyDataSetChanged();
                    } else if (contains(ds.getKey(), orders) != orders.size()) {
                        orders.remove(contains(ds.getKey(), orders));
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
                setAddress(position);
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
        setDateTime();
    }

    public int contains(String s, ArrayList<Order> orders) {
        int b = orders.size();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).ordinance.equals(s)) {
                b = i;
            } else {
                i++;
            }
        }
        return b;
    }

    public void setDateTime() {

        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    public void setAddress(final int position) {
        if (orders.get(position).state.equals("1")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OrdersActivity.this)
                    .setTitle("")
                    .setPositiveButton(getString(R.string.set_address), new DialogInterface.OnClickListener() {
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

    class Order {

        String ordinance;
        String ph_ID;
        String date;
        String time;
        String state;
        String note;
        String total;

        Order(String ordinance, String ph_ID, String date, String time, String state, String note, String total) {
            this.ordinance = ordinance;
            this.ph_ID = ph_ID;
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
            final TextView ph_Name = view.findViewById(R.id.ord_ph_name);
            TextView date = view.findViewById(R.id.ord_date);
            TextView time = view.findViewById(R.id.ord_time);
            TextView state = view.findViewById(R.id.ord_state);
            TextView total = view.findViewById(R.id.ord_total);
            TextView note = view.findViewById(R.id.ord_note);
            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Pharmacy");
            reference1.child(Objects.requireNonNull(Objects.requireNonNull(getItem(position)).ph_ID))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ph_Name.setText(dataSnapshot.child("Name").getValue(String.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
            ;
            date.setText(Objects.requireNonNull(getItem(position)).date);
            time.setText(Objects.requireNonNull(getItem(position)).time);
            note.setText(Objects.requireNonNull(getItem(position)).note);
            total.setText(Objects.requireNonNull(getItem(position)).total);

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
}


