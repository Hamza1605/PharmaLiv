package com.example.pharmaliv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class ClientsRequestsActivity extends AppCompatActivity {

    DatabaseReference ordinanceReference;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clients_requests);

        final ArrayList<Prescription> prescriptions = new ArrayList<>();
        final RequestAdapter requestAdapter = new RequestAdapter(ClientsRequestsActivity.this, prescriptions);
        ListView listView = findViewById(R.id.requests_list);
        ordinanceReference = FirebaseDatabase.getInstance().getReference().child("Prescription");
        listView.setAdapter(requestAdapter);
        user = FirebaseAuth.getInstance().getCurrentUser();
        ordinanceReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                prescriptions.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Prescription prescription = ds.getValue(Prescription.class);
                    if (Objects.equals(Objects.requireNonNull(prescription).getPharmacy_ID(), "ph" + user.getUid())) {
                        if ((Objects.equals(prescription.getState(), "0"))
                                || (Objects.equals(prescription.getState(), "3"))
                                || (Objects.equals(prescription.getState(), "5"))
                                || (Objects.equals(prescription.getState(), "7"))) {
                            prescriptions.add(prescription);
                            requestAdapter.notifyDataSetChanged();
                        }
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
                Intent intent = new Intent(ClientsRequestsActivity.this, ClientRequestActivity.class);
                intent.putExtra("req_ID", prescriptions.get(position).getId());
                intent.putExtra("req_client_ID", prescriptions.get(position).getClient_ID());
                intent.putExtra("req_state", String.valueOf(prescriptions.get(position).getState()));
                intent.putExtra("req_total", String.valueOf(prescriptions.get(position).getTotal()));
                startActivity(intent);
            }
        });
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

    class RequestAdapter extends ArrayAdapter<Prescription> {

        RequestAdapter(@NonNull Context context, ArrayList<Prescription> prescriptions) {
            super(context, 0, prescriptions);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.request_row,
                        parent, false);
            final TextView client_name = view.findViewById(R.id.req_client_name);
            TextView date = view.findViewById(R.id.req_date);
            TextView time = view.findViewById(R.id.req_time);
            TextView add = view.findViewById(R.id.req_add);
            TextView del_date = view.findViewById(R.id.req_dt);
            TextView del_time = view.findViewById(R.id.req_tm);


            DatabaseReference clientReference = FirebaseDatabase.getInstance().getReference("Client");
            clientReference.child(Objects.requireNonNull(getItem(position)).getClient_ID())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Client client = dataSnapshot.getValue(Client.class);
                                String s = Objects.requireNonNull(client).getFamily_Name() + " " +
                                        Objects.requireNonNull(client).getFirst_Name();
                                client_name.setText(s);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

            date.setText(Objects.requireNonNull(getItem(position)).getSending_Date());
            time.setText(Objects.requireNonNull(getItem(position)).getSending_Time());
            if (Objects.requireNonNull(getItem(position)).getLatitude() != null &&
                    Objects.requireNonNull(getItem(position)).getLongitude() != null) {
                add.setText(getString(R.string.address_def));
            }
            if (Objects.requireNonNull(getItem(position)).getDelivery_Date() != null &&
                    Objects.requireNonNull(getItem(position)).getDelivery_Date() != null) {
                del_date.setText(Objects.requireNonNull(getItem(position)).getDelivery_Date());
                del_time.setText(Objects.requireNonNull(getItem(position)).getDelivery_Time());
            }
            return view;
        }
    }
}
