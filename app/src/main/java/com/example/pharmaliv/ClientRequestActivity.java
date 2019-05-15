package com.example.pharmaliv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class ClientRequestActivity extends AppCompatActivity {

    private DatabaseReference ordinanceReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_request);

        final TextView clName = findViewById(R.id.rec_cl_name);
        final EditText total = findViewById(R.id.req_totall);
        final Button accept = findViewById(R.id.req_accept);
        final Button decline = findViewById(R.id.req_decline);
        final ImageView req_img = findViewById(R.id.req_img);
        final ListView listView = findViewById(R.id.req_med_list);
        final ArrayList<Medication> medications = new ArrayList<>();
        final MedicationAdapter medicationAdapter =
                new MedicationAdapter(ClientRequestActivity.this, medications);
        listView.setAdapter(medicationAdapter);

        if ((getIntent().getStringExtra("req_client_ID").equals("3"))) {
            accept.setText(getString(R.string.select_delivery));
        }

        DatabaseReference clientReference = FirebaseDatabase.getInstance().getReference("Client");
        clientReference.child(getIntent().getStringExtra("req_client_ID"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String[] s = new String[1];
                            s[0] = dataSnapshot.child("Family Name").getValue(String.class) + " " +
                                    dataSnapshot.child("First Name").getValue(String.class);
                            clName.setText(s[0]);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });


        ordinanceReference = FirebaseDatabase.getInstance().getReference().child("Ordinance")
                .child(getIntent().getStringExtra("req_ID"));
        ordinanceReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("med_nbr").exists()) {
                    listView.setVisibility(View.VISIBLE);
                    for (DataSnapshot ds : dataSnapshot.child("Medication").getChildren()) {
                        medications.add(new Medication(ds.getKey(), ds.getValue(Integer.class)));
                        medicationAdapter.notifyDataSetChanged();
                    }
                } else if (dataSnapshot.child("image").exists()) {
                    req_img.setVisibility(View.VISIBLE);
                    StorageReference imageReference = FirebaseStorage.getInstance().getReference("Ordinance")
                            .child(Objects.requireNonNull(dataSnapshot.child("image").getValue(String.class)));
                    imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
                if (!TextUtils.isEmpty(total.getText().toString())) {
                    ordinanceReference.child("State").setValue("1");
                    ordinanceReference.child("Total").setValue(total.getText().toString() + " DA");
                    final EditText editText = new EditText(ClientRequestActivity.this);
                    AlertDialog dialog = new AlertDialog.Builder(ClientRequestActivity.this)
                            .setTitle(getString(R.string.set_note))
                            .setView(editText)
                            .setPositiveButton(getString(R.string.set_note), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!TextUtils.isEmpty(editText.getText().toString())) {
                                        ordinanceReference.child("Note Client").setValue(editText.getText().toString());
                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create();
                    dialog.show();
                    accept.setEnabled(false);
                    decline.setEnabled(false);
                } else {
                    total.setError(getString(R.string.put_total));
                }
            }
        });

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((getIntent().getStringExtra("req_client_ID").equals("3"))) {
                    Intent intent = new Intent(ClientRequestActivity.this, DeliveryMenActivity.class);
                    intent.putExtra("send", 1);
                    startActivityForResult(intent, 1);
                } else {
                    ordinanceReference.child("State").setValue("2");
                    final EditText editText = new EditText(ClientRequestActivity.this);
                    AlertDialog dialog = new AlertDialog.Builder(ClientRequestActivity.this)
                            .setTitle(getString(R.string.set_note))
                            .setView(editText)
                            .setPositiveButton(getString(R.string.set_note), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (!TextUtils.isEmpty(editText.getText().toString())) {
                                        ordinanceReference.child("Note Client").setValue(editText.getText().toString());
                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create();
                    dialog.show();
                    accept.setEnabled(false);
                    decline.setEnabled(false);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            ordinanceReference.child("Delivery Man").setValue(Objects.requireNonNull(data)
                    .getStringExtra("Delivery Man"));
            final EditText editText = new EditText(ClientRequestActivity.this);
            AlertDialog dialog = new AlertDialog.Builder(ClientRequestActivity.this)
                    .setTitle(getString(R.string.set_note))
                    .setView(editText)
                    .setPositiveButton(getString(R.string.set_note), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (!TextUtils.isEmpty(editText.getText().toString())) {
                                ordinanceReference.child("Note Delivery Man").setValue(editText.getText().toString());
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create();
            dialog.show();
        }
    }
}
