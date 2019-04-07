package com.example.pharmaliv;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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


public class ClientsRequestsFragment extends Fragment {

    DatabaseReference reference;
    FirebaseUser user;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients_requests, container, false);
        final ArrayList<ClientRequest> clientRequests = new ArrayList<>();
        final RequestAdapter requestAdapter = new RequestAdapter(Objects.requireNonNull(getContext()), clientRequests);
        ListView listView = view.findViewById(R.id.requests_list);
        listView.setAdapter(requestAdapter);
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Ordinance");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Client");
                if ((Objects.equals(dataSnapshot.child("Pharmacy").getValue(String.class), "ph" + user.getUid()))
                        && (Objects.equals(dataSnapshot.child("statue").getValue(String.class), "0"))) {
                    final String[] s1 = new String[1];
                    final String[] s2 = new String[1];
                    reference.child(Objects.requireNonNull(dataSnapshot.child("Client")
                            .getValue(String.class))).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            s1[0] = snapshot.child("Family Name").getValue(String.class);
                            s2[0] = snapshot.child("First Name").getValue(String.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    clientRequests.add(new ClientRequest(dataSnapshot.getKey(),
                            dataSnapshot.child("Client").getValue(String.class),
                            s1[0] + " " + s2[0],
                            dataSnapshot.child("Date").getValue(String.class),
                            dataSnapshot.child("Time").getValue(String.class)));
                    requestAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ClientsRequestsFragment.class);
                intent.putExtra("req_ID", clientRequests.get(position).rec_ID);
                intent.putExtra("req_Name", clientRequests.get(position).clientName);
                startActivity(intent);
            }
        });

        return view;
    }

}

class ClientRequest {

    String rec_ID;
    String client_ID;
    String clientName;
    String req_date;
    String req_time;

    ClientRequest(String rec_ID, String client_ID, String clientName, String req_date, String req_time) {
        this.rec_ID = rec_ID;
        this.client_ID = client_ID;
        this.clientName = clientName;
        this.req_date = req_date;
        this.req_time = req_time;
    }
}

class RequestAdapter extends ArrayAdapter<ClientRequest> {

    RequestAdapter(@NonNull Context context, ArrayList<ClientRequest> medications) {
        super(context, 0, medications);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.request_row,
                    parent, false);
        TextView client_name = view.findViewById(R.id.req_client_name);
        TextView date = view.findViewById(R.id.req_date);
        TextView time = view.findViewById(R.id.req_time);
        client_name.setText(Objects.requireNonNull(getItem(position)).clientName);
        date.setText(Objects.requireNonNull(getItem(position)).req_date);
        time.setText(Objects.requireNonNull(getItem(position)).req_time);
        return view;
    }
}