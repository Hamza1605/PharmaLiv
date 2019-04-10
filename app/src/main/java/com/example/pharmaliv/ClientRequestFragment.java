package com.example.pharmaliv;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;


public class ClientRequestFragment extends Fragment {


    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_request, container, false);
        TextView clName = view.findViewById(R.id.rec_cl_name);
        EditText total = view.findViewById(R.id.req_totol);
        Button accept = view.findViewById(R.id.req_accept);
        Button decline = view.findViewById(R.id.req_decline);
        final ImageView req_img = view.findViewById(R.id.req_img);
        ListView listView = view.findViewById(R.id.req_med_list);
        final ArrayList<Medication> medications = new ArrayList<>();
        final MedicationAdapter medicationAdapter = new MedicationAdapter(Objects.requireNonNull(getContext()), medications);
        listView.setAdapter(medicationAdapter);

        clName.setText(Objects.requireNonNull(getActivity()).getIntent().getStringExtra("req_Name"));
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Ordinance")
                .child(Objects.requireNonNull(getActivity()).getIntent().getStringExtra("req_ID"));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("med_nbr").exists()) {
                    DatabaseReference r = reference.child("Medication");
                    int nbr = dataSnapshot.child("med_nbr").getValue(Integer.class);
                    r.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            DatabaseReference rm = FirebaseDatabase.getInstance().getReference().child("Medication");
                            final String[] s = new String[1];
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                rm.child(Objects.requireNonNull(ds.getKey())).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        s[0] = dataSnapshot.child("Name").getValue(String.class);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                medications.add(new Medication(s[0], ds.getValue(String.class)));
                                medicationAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else if (dataSnapshot.child("image").exists()) {
                    StorageReference sRef = FirebaseStorage.getInstance().getReference().child("Ordinance")
                            .child(Objects.requireNonNull(reference.getKey()));
                    final Uri uri = null;
                    sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            req_img.setImageURI(uri);
                        }
                    });
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
