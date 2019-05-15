package com.example.pharmaliv;

import android.content.Context;
import android.content.Intent;
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

        final ArrayList<ClientRequest> clientRequests = new ArrayList<>();
        final RequestAdapter requestAdapter = new RequestAdapter(ClientsRequestsActivity.this, clientRequests);
        ListView listView = findViewById(R.id.requests_list);
        listView.setAdapter(requestAdapter);
        user = FirebaseAuth.getInstance().getCurrentUser();
        ordinanceReference = FirebaseDatabase.getInstance().getReference().child("Ordinance");
        ordinanceReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if ((Objects.equals(ds.child("Pharmacy").getValue(String.class), "ph" + user.getUid()))
                            && ((Objects.equals(ds.child("State").getValue(String.class), "0"))
                            || (Objects.equals(ds.child("State").getValue(String.class), "3")))) {
                        if (contains(ds.getKey(), clientRequests) != clientRequests.size()) {
                            clientRequests.set(contains(ds.getKey(), clientRequests),
                                    new ClientRequest(
                                            ds.getKey(),
                                            ds.child("Client").getValue(String.class),
                                            ds.child("State").getValue(String.class),
                                            ds.child("Date").getValue(String.class),
                                            ds.child("Time").getValue(String.class),
                                            ds.child("Address").exists(),
                                            ds.child("Delivery Date").getValue(String.class) + " " +
                                                    ds.child("Delivery Time").getValue(String.class)
                                    ));
                        } else {
                            clientRequests.add(new ClientRequest(
                                    ds.getKey(),
                                    ds.child("Client").getValue(String.class),
                                    null,
                                    ds.child("Date").getValue(String.class),
                                    ds.child("Time").getValue(String.class),
                                    ds.child("Address").exists(),
                                    ds.child("Delivery Date").getValue(String.class) + " " +
                                            ds.child("Delivery Time").getValue(String.class)));
                        }
                        requestAdapter.notifyDataSetChanged();
                    } else {
                        if (contains(ds.getKey(), clientRequests) != clientRequests.size()) {
                            clientRequests.remove(contains(ds.getKey(), clientRequests));
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
                intent.putExtra("req_ID", clientRequests.get(position).req_ID);
                intent.putExtra("req_client_ID", clientRequests.get(position).client_ID);
                intent.putExtra("req_state", clientRequests.get(position).req_state);
                startActivity(intent);
            }
        });
    }

    public int contains(String s, ArrayList<ClientRequest> clientRequests) {
        int b = clientRequests.size();
        for (int i = 0; i < clientRequests.size(); i++) {
            if (clientRequests.get(i).req_ID.equals(s)) {
                b = i;
                break;
            } else {
                i++;
            }
        }
        return b;
    }

    class ClientRequest {

        String req_ID;
        String client_ID;
        String req_state;
        String req_date;
        String req_time;
        boolean req_add;
        String req_del;

        ClientRequest(String req_ID, String client_ID, String req_state, String req_date, String req_time,
                      boolean req_add, String req_del) {
            this.req_ID = req_ID;
            this.client_ID = client_ID;
            this.req_state = req_state;
            this.req_date = req_date;
            this.req_time = req_time;
            this.req_add = req_add;
            this.req_del = req_del;
        }
    }

    class RequestAdapter extends ArrayAdapter<ClientRequest> {

        RequestAdapter(@NonNull Context context, ArrayList<ClientRequest> medications) {
            super(context, 0, medications);
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
            TextView del = view.findViewById(R.id.req_del);


            DatabaseReference clientReference = FirebaseDatabase.getInstance().getReference("Client");
            clientReference.child(Objects.requireNonNull(getItem(position)).client_ID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String[] s = new String[1];
                                s[0] = dataSnapshot.child("Family Name").getValue(String.class) + " " +
                                        dataSnapshot.child("First Name").getValue(String.class);
                                client_name.setText(s[0]);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

            date.setText(Objects.requireNonNull(getItem(position)).req_date);
            time.setText(Objects.requireNonNull(getItem(position)).req_time);
            if (Objects.requireNonNull(getItem(position)).req_add) {
                add.setVisibility(View.VISIBLE);
                add.setText(getString(R.string.address_def));
            }
            del.setText(Objects.requireNonNull(getItem(position)).req_del);
            return view;
        }
    }
}
