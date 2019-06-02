package com.example.pharmaliv;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng l;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnMapData(l);
                finish();
            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        fab.setVisibility(View.INVISIBLE);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (this.getIntent().getStringExtra("send").equals("0")) {
            LatLng l1 = new LatLng(this.getIntent().getDoubleExtra("llat", 0),
                    this.getIntent().getDoubleExtra("llng", 0));
            LatLng l2 = new LatLng(this.getIntent().getDoubleExtra("mlat", 0),
                    this.getIntent().getDoubleExtra("mlng", 0));
            mMap.clear();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l2, 15));
            mMap.addMarker(new MarkerOptions().position(l1).title("My Location"));
            mMap.addMarker(new MarkerOptions().position(l2).title("Delivery Location"));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.404122, 8.124354), 15));
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                    l = latLng;
                    fab.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void ReturnMapData(LatLng latLng) {
        Intent intent = this.getIntent();
        intent.putExtra("latitude", latLng.latitude);
        intent.putExtra("longitude", latLng.longitude);
        this.setResult(RESULT_OK, intent);
    }
}
