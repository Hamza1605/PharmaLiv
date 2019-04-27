package com.example.pharmaliv;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;


public class MissionFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mission, container, false);
        TextView phName = view.findViewById(R.id.mf_ph_name);
        TextView clName = view.findViewById(R.id.mf_cl_name);
        MapView mapView = view.findViewById(R.id.mapView);
        Button acc = view.findViewById(R.id.mf_acc);
        Button dec = view.findViewById(R.id.mf_dec);


        return view;
    }
}
