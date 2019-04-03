package com.example.pharmaliv;

import android.content.Context;
import android.content.DialogInterface;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
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
        final ArrayList<String> meds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Medication");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    meds.add(dataSnapshot.child("Name").getValue(String.class));
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
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, meds);
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
                                                medicationList.set(position, new Medication(medicationList.get(position).name,
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
                if ((!autoCompletemedName.getText().toString().isEmpty()) && (!editTextmedQuantity.getText().toString().isEmpty())
                        && (Integer.valueOf(editTextmedQuantity.getText().toString()) > 0)) {
                    medicationList.add(new Medication(autoCompletemedName.getText().toString(), editTextmedQuantity.getText().toString()));
                    adapter.notifyDataSetChanged();
                    autoCompletemedName.setText("");
                    editTextmedQuantity.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "Medication Name case, Quantity case are empty or negative value",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}

class Medication {
    String name;
    String quantity;

    Medication(String designation, String quantity){
        this.name = designation;
        this.quantity = quantity;
    }
}

class MedicationAdapter extends ArrayAdapter<Medication> {

    MedicationAdapter(@NonNull Context context, ArrayList<Medication> medications) {
        super(context, 0, medications);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2
                    , parent, false);
        TextView name = view.findViewById(android.R.id.text1);
        TextView quntity = view.findViewById(android.R.id.text2);
        name.setText(Objects.requireNonNull(getItem(position)).name);
        quntity.setText(Objects.requireNonNull(getItem(position)).quantity);
        return view;
    }
}
