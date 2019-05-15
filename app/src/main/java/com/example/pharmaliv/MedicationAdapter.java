package com.example.pharmaliv;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

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
        name.setText(Objects.requireNonNull(getItem(position)).getName());
        quntity.setText(String.valueOf(Objects.requireNonNull(getItem(position)).getQuantity()));
        return view;
    }
}
