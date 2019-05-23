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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class OrdersActivity extends AppCompatActivity {

    private String s;
    private DatabaseReference reference;
    private ArrayList<Prescription> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        final String Uid = getIntent().getStringExtra("Uid");
        orders = new ArrayList<>();
        final OrdersAdapter adapter = new OrdersAdapter(this, orders);
        reference = FirebaseDatabase.getInstance().getReference().child("Prescription");
        ListView listView = findViewById(R.id.orders_list);
        listView.setAdapter(adapter);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Prescription prescription = ds.getValue(Prescription.class);
                    if ((Objects.equals(ds.child("client_ID").getValue(String.class), Uid))
                            && (!Objects.equals(Objects.requireNonNull(ds.child("state").getValue()).toString(), "8"))) {
                        if (contains(ds.getKey(), orders) != orders.size()) {
                            orders.set(contains(ds.getKey(), orders), prescription);
                        } else {
                            orders.add(prescription);
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
                Toast.makeText(getApplicationContext(), orders.get(position).getState(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            reference.child(s).child("latitude")
                    .setValue(String.valueOf(Objects.requireNonNull(data).getDoubleExtra("latitude", 0)));
            reference.child(s).child("longitude")
                    .setValue(String.valueOf(Objects.requireNonNull(data).getDoubleExtra("longitude", 0)));
            reference.child(s).child("state").setValue("3");
        }
        setDeliveryDateTime();
    }

    public int contains(String s, ArrayList<Prescription> orders) {
        int b = orders.size();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId().equals(s)) {
                b = i;
                break;
            } else {
                i++;
            }
        }
        return b;
    }

    public void setDeliveryDateTime() {

        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String s1 = null;
                        if (monthOfYear < 10 && dayOfMonth < 10)
                            s1 = year + "-0" + monthOfYear + "-0" + dayOfMonth;
                        else if (monthOfYear < 10)
                            s1 = year + "-0" + monthOfYear + "-" + dayOfMonth;
                        else if (dayOfMonth < 10)
                            s1 = year + "-0" + monthOfYear + "-" + dayOfMonth;
                        reference.child(s).child("delivery_Date").setValue(s1);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String s1 = null;
                        if (hourOfDay < 10 && minute < 10)
                            s1 = "0" + hourOfDay + ":0" + minute;
                        else if (hourOfDay < 10)
                            s1 = "0" + hourOfDay + ":" + minute;
                        else if (minute < 10)
                            s1 = hourOfDay + ":0" + minute;
                        reference.child(s).child("delivery_Time").setValue(s1);
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    public void setAddress(final int position) {
        if (orders.get(position).getState().equals("1")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OrdersActivity.this)
                    .setTitle("")
                    .setPositiveButton(getString(R.string.set_address), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            s = orders.get(position).getId();
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

    class OrdersAdapter extends ArrayAdapter<Prescription> {

        OrdersAdapter(@NonNull Context context, ArrayList<Prescription> orders) {
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
            reference1.child(Objects.requireNonNull(Objects.requireNonNull(getItem(position)).getPharmacy_ID()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ph_Name.setText(dataSnapshot.child("name").getValue(String.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
            ;
            date.setText(Objects.requireNonNull(getItem(position)).getSending_Date());
            time.setText(Objects.requireNonNull(getItem(position)).getSending_Time());
            if (Objects.requireNonNull(getItem(position)).getClient_Note() != null)
                note.setText(Objects.requireNonNull(getItem(position)).getClient_Note());
            if (Objects.requireNonNull(getItem(position)).getTotal() != null)
                total.setText(Objects.requireNonNull(getItem(position)).getTotal() + " DA");

            switch (Objects.requireNonNull(getItem(position)).getState()) {
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


