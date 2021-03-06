package com.example.pharmaliv;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ScanActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int GALLERY_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    private static final int PHARMACY_LIST_CODE = 50;

    private Uri uri, resultUri;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        imageView = findViewById(R.id.ImageView);
        Button buttonCamera = findViewById(R.id.camera);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                        &&(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED))
                    ActivityCompat.requestPermissions((Activity) getApplicationContext(),
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            CAMERA_REQUEST_CODE);
                else
                    pickCamera();
            }
        });

        Button buttonGallery = findViewById(R.id.gallery);
        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},GALLERY_REQUEST_CODE);
                else
                    pickGallery();
            }
        });

        Button buttonSelect = findViewById(R.id.selectscan);
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPharmacy();
            }
        });
    }

    void selectPharmacy() {
        if (resultUri != null)
            startActivityForResult(new Intent(ScanActivity.this, PharmacyListActivity.class),
                    PHARMACY_LIST_CODE);
        else
            Toast.makeText(ScanActivity.this, getString(R.string.no_image), Toast.LENGTH_SHORT).show();
    }

    void pickCamera (){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "PharmaLivPic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Ordinance Scanned");
        uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    void pickGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE :
                if ((grantResults[0] != PackageManager.PERMISSION_GRANTED)&&(grantResults[1] != PackageManager.PERMISSION_GRANTED))
                    Toast.makeText(this,"Permission denied!", Toast.LENGTH_SHORT).show();
                else
                    pickCamera();
                break;

            case GALLERY_REQUEST_CODE :
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this,"Permission denied!", Toast.LENGTH_SHORT).show();
                else
                    pickGallery();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Date currentTime = Calendar.getInstance().getTime();
        if (requestCode == PHARMACY_LIST_CODE && resultCode == Activity.RESULT_OK) {
            DatabaseReference prescriptionReference = FirebaseDatabase.getInstance().getReference().child("Prescription").push();
            Prescription prescription = new Prescription(
                    prescriptionReference.getKey(),
                    "cl" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                    Objects.requireNonNull(data).getStringExtra("Ph_ID"),
                    "0",
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime),
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime),
                    null, null, null, null, null, null, null);

            prescriptionReference.setValue(prescription);
            StorageReference sRef = FirebaseStorage.getInstance().getReference().child("Prescription")
                    .child(Objects.requireNonNull(prescriptionReference.getKey()));
            sRef.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No pharmacy selected", Toast.LENGTH_SHORT).show();
        }

        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                CropImage.activity(Objects.requireNonNull(data).getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                CropImage.activity(uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = Objects.requireNonNull(result).getUri();
                imageView.setImageURI(resultUri);
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception e = Objects.requireNonNull(result).getError();
                Toast.makeText(this, "error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
