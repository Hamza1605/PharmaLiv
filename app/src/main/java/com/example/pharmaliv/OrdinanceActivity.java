package com.example.pharmaliv;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class OrdinanceActivity extends AppCompatActivity {

    ArrayList<Medication> medicationList;
    MedicationAdapter adapter;
    AutoCompleteTextView autoCompleteName;
    EditText editTextMedQuantity;
    ListView medicationsListView;
    ArrayList<Medication> meds;
    ArrayList<String> med;
    DatabaseReference medicationReference;
    int[] selectedItemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordinance);
        meds = new ArrayList<>();
        med = new ArrayList<>();
        downloadListMeds();

        medicationList = new ArrayList<>();
        adapter = new MedicationAdapter(this, medicationList);
        medicationsListView = findViewById(R.id.medicationsListView);
        autoCompleteName = findViewById(R.id.medname);
        editTextMedQuantity = findViewById(R.id.medqauntity);
        Button buttonAdd = findViewById(R.id.add);
        Button buttonSelect = findViewById(R.id.selectphord);
        medicationsListView.setAdapter(adapter);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(OrdinanceActivity.this,
                android.R.layout.select_dialog_item, med);
        autoCompleteName.setAdapter(arrayAdapter);
        autoCompleteName.setThreshold(1);

        selectedItemPosition = new int[]{0};
        autoCompleteName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItemPosition[0] = position;
            }
        });

        medicationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                updateList(position);
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMedication();
            }
        });

        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPharmacy();

            }
        });
    }

    private void selectPharmacy() {
        if (!medicationList.isEmpty())
            startActivityForResult(new Intent(OrdinanceActivity.this, PharmacyListActivity.class), 54);
        else
            Toast.makeText(OrdinanceActivity.this, getString(R.string.empty_medication_list), Toast.LENGTH_SHORT).show();
    }

    private void addMedication() {
        if ((!autoCompleteName.getText().toString().isEmpty()) && (!editTextMedQuantity.getText().toString().isEmpty())
                && (Integer.valueOf(editTextMedQuantity.getText().toString()) > 0)) {
            if (med.contains(autoCompleteName.getText().toString())) {
                if (!contains(autoCompleteName.getText().toString(), medicationList)) {
                    medicationList.add(new Medication(
                            meds.get(selectedItemPosition[0]).getID(),
                            autoCompleteName.getText().toString(),
                            Integer.parseInt(editTextMedQuantity.getText().toString())));
                    adapter.notifyDataSetChanged();
                    autoCompleteName.setText("");
                    editTextMedQuantity.setText("0");
                } else {
                    Toast.makeText(getApplicationContext(), "This medication is  exist in the list",
                            Toast.LENGTH_SHORT).show();
                    autoCompleteName.setText("");
                    editTextMedQuantity.setText("0");
                }
            } else {
                Toast.makeText(getApplicationContext(), "This medication is not exist in database",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Medication Name case, Quantity case are empty or negative value"
                    , Toast.LENGTH_SHORT).show();
        }
    }

    private void updateList(final int position) {

        AlertDialog dialog = new AlertDialog.Builder(OrdinanceActivity.this)
                .setTitle(getString(R.string.select_action))
                .setPositiveButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText editText = new EditText(OrdinanceActivity.this);
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setSingleLine();
                        AlertDialog alertDialog = new AlertDialog.Builder(OrdinanceActivity.this)
                                .setTitle(getString(R.string.add_medication))
                                .setView(editText)
                                .setPositiveButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (TextUtils.isEmpty(editText.getText())) {
                                            medicationList.get(position).setQuantity(
                                                    Integer.parseInt(editText.getText().toString()));
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                })
                                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create();
                        alertDialog.show();
                    }
                })
                .setNegativeButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        medicationList.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                })
                .create();
        dialog.show();

    }

    boolean contains(String s, ArrayList<Medication> medications) {
        boolean b = false;
        for (int i = 0; i < medications.size(); i++) {
            if (medications.get(i).getName().equals(s)) {
                b = true;
            } else {
                i++;
            }
        }
        return b;
    }

    public void downloadListMeds() {
        medicationReference = FirebaseDatabase.getInstance().getReference("Medication");
        medicationReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Medication m = ds.getValue(Medication.class);
                    meds.add(new Medication(m.getID(), m.getName()));
                    med.add(m.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 54 && resultCode == Activity.RESULT_OK) {
            Date currentTime = Calendar.getInstance().getTime();
            DatabaseReference ordinanceReference = FirebaseDatabase.getInstance().getReference().child("Prescription").push();
            Prescription prescription = new Prescription(ordinanceReference.getKey(),
                    "cl" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                    Objects.requireNonNull(data).getStringExtra("Ph_ID"),
                    "0",
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime),
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime),
                    null, null, null, null, null, null, null);

            ordinanceReference.setValue(prescription);
            for (int i = 0; i < medicationList.size(); i++) {
                ordinanceReference.child("Medication").child((medicationList.get(i).getName()))
                        .setValue(medicationList.get(i).getQuantity());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No pharmacy selected", Toast.LENGTH_SHORT).show();
        }
    }
}
