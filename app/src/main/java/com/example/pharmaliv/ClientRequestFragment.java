package com.example.pharmaliv;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class ClientRequestFragment extends Fragment {


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_request, container, false);
        TextView clName = view.findViewById(R.id.rec_cl_name);
        EditText total = view.findViewById(R.id.req_totol);
        Button accept = view.findViewById(R.id.req_accept);
        Button decline = view.findViewById(R.id.req_decline);
        ListView listView = view.findViewById(R.id.req_med_list);
        final ArrayList<Medication> medications = new ArrayList<>();
        final MedicationAdapter medicationAdapter = new MedicationAdapter(Objects.requireNonNull(getContext()), medications);
        listView.setAdapter(medicationAdapter);

        clName.setText(Objects.requireNonNull(getActivity()).getIntent().getStringExtra("req_Name"));
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Ordinance")
                .child(Objects.requireNonNull(getActivity()).getIntent().getStringExtra("req_ID"));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int nbr = dataSnapshot.child("med_nbr").getValue(Integer.class);
                for (int i = 0; i < nbr; i++) {
                    medications.add(new Medication(dataSnapshot.child(String.valueOf(i)).child("Name").getValue(String.class),
                            dataSnapshot.child(String.valueOf(i)).child("Quantity").getValue(String.class)));
                    medicationAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }
}
