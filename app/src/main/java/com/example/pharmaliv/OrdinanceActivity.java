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
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class OrdinanceActivity extends AppCompatActivity {

    ArrayList<Medication> medicationList;
    MedicationAdapter adapter;
    AutoCompleteTextView autoCompletemedName;
    EditText editTextmedQuantity;
    ListView medications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordinance);
        final ArrayList<Medication> meds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Medication");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    meds.add(new Medication(dataSnapshot.getKey(), ds.child("Name").getValue(String.class)));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        medicationList = new ArrayList<>();
        adapter = new MedicationAdapter(this, medicationList);
        medications = findViewById(R.id.medicationsListView);
        autoCompletemedName = findViewById(R.id.medname);
        editTextmedQuantity = findViewById(R.id.medqauntity);
        Button buttonAdd = findViewById(R.id.add);
        Button buttonSelect = findViewById(R.id.select);
        medications.setAdapter(adapter);
        final ArrayList<String> med = new ArrayList<>();
        for (int i = 0; i < meds.size(); i++) {
            med.add(meds.get(i).name);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, med);
        autoCompletemedName.setAdapter(arrayAdapter);
        medications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                TextView name = view.findViewById(android.R.id.text1);
                TextView quantity1 = view.findViewById(android.R.id.text2);
                final AlertDialog.Builder dialog = new AlertDialog.Builder(OrdinanceActivity.this);
                        dialog.setTitle("Please select action.")
                        .setMessage(name.getText().toString()+": "+quantity1.getText().toString())
                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final TextView quantity = new EditText(OrdinanceActivity.this);
                                final AlertDialog.Builder editDialog = new AlertDialog.Builder(OrdinanceActivity.this)
                                        .setTitle("Edit")
                                        .setView(quantity)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                quantity.setText(medicationList.get(position).quantity);
                                                quantity.setPadding(16, 0, 16,0);
                                                quantity.setInputType(InputType.TYPE_CLASS_NUMBER);
                                                quantity.setSingleLine();
                                                medicationList.set(position, new Medication(medicationList.get(position).med_id,
                                                        medicationList.get(position).name,
                                                        quantity.getText().toString()));
                                                adapter.notifyDataSetChanged();
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                editDialog.show();
                            }
                        })
                        .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                medicationList.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        });
                dialog.show();
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (med.contains(autoCompletemedName.getText().toString())) {
                    if ((!autoCompletemedName.getText().toString().isEmpty()) && (!editTextmedQuantity.getText().toString().isEmpty())
                            && (Integer.valueOf(editTextmedQuantity.getText().toString()) > 0)) {
                        medicationList.add(new Medication(meds.get(autoCompletemedName.getListSelection()).med_id,
                                autoCompletemedName.getText().toString(), editTextmedQuantity.getText().toString()));
                        adapter.notifyDataSetChanged();
                        autoCompletemedName.setText("");
                        editTextmedQuantity.setText("");
                    } else {
                        Toast.makeText(getApplicationContext(), "Medication Name case, Quantity case are empty or negative value",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "This medication is not exist in database", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!medicationList.isEmpty())
                    startActivityForResult(new Intent(OrdinanceActivity.this, PharmacyListActivity.class), 1);
                else
                    Toast.makeText(OrdinanceActivity.this, getString(R.string.empty_medication_list), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Ordinance").push();
            ref.child("Pharmacy").setValue(Objects.requireNonNull(data).getStringExtra("Ph_ID"));
            ref.child("Client").setValue("cl" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            ref.child("statue").setValue("0");
            ref.child("Date").setValue(new SimpleDateFormat("dd-mm-yyyy").format(Calendar.getInstance().getTime()));
            ref.child("Time").setValue(new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()));
            ref.child("med_nbr").setValue(medicationList.size());
            for (int i = 0; i < medicationList.size(); i++) {
                ref.child(String.valueOf(i)).child("Name").setValue(medicationList.get(i).name);
                ref.child(String.valueOf(i)).child("Quantity").setValue(medicationList.get(i).quantity);
            }
        } else {
            Toast.makeText(getApplicationContext(), "No pharmacy selected", Toast.LENGTH_SHORT).show();
        }
    }
}

