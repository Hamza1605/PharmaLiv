package com.example.pharmaliv;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
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
        fab.setEnabled(false);
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
        if (this.getIntent().getStringExtra("send").equals("0")) {
            LatLng l1 = new LatLng(this.getIntent().getDoubleExtra("llat", 0),
                    this.getIntent().getDoubleExtra("llng", 0));
            LatLng l2 = new LatLng(this.getIntent().getDoubleExtra("mlat", 0),
                    this.getIntent().getDoubleExtra("mlng", 0));
            mMap.clear();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l2, 15));
            mMap.addMarker(new MarkerOptions().position(l1).title("My Location"));
            mMap.addMarker(new MarkerOptions().position(l2).title("Delivery Location"));

            GoogleDirection.withServerKey("AIzaSyC25hO3DAkFhrKLTtdmpqFV5UujkQo6RNg")
                    .from(l1)
                    .to(l2)
                    .avoid(AvoidType.FERRIES)
                    .avoid(AvoidType.HIGHWAYS)
                    .transportMode(TransportMode.DRIVING)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            if (direction.isOK()) {
                                ArrayList<LatLng> directionPositionList = direction.getRouteList()
                                        .get(0).getLegList().get(0).getDirectionPoint();
                                mMap.addPolyline(DirectionConverter.createPolyline(
                                        MapsActivity.this, directionPositionList, 5, Color.RED));
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                        }
                    });
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.404122, 8.124354), 15));
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                    l = latLng;
                    fab.setEnabled(true);
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
