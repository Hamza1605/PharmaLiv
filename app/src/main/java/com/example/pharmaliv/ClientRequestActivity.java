package com.example.pharmaliv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class ClientRequestActivity extends AppCompatActivity {

    private DatabaseReference ordinanceReference;
    private EditText total;
    private Button accept;
    private Button decline;
    private TextView clName;
    private ImageView req_img;
    private ListView listView;
    private ArrayList<Medication> medications;
    private MedicationAdapter medicationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_request);

        clName = findViewById(R.id.rec_cl_name);
        total = findViewById(R.id.req_totall);
        accept = findViewById(R.id.req_accept);
        decline = findViewById(R.id.req_decline);
        req_img = findViewById(R.id.req_img);
        listView = findViewById(R.id.req_med_list);
        medications = new ArrayList<>();
        medicationAdapter = new MedicationAdapter(ClientRequestActivity.this, medications);
        listView.setAdapter(medicationAdapter);

        if (getIntent().getStringExtra("req_state").equals("3")) {
            accept.setText(getString(R.string.select_delivery));
            total.setVisibility(View.GONE);
        } else if (getIntent().getStringExtra("req_state").equals("5")) {
            decline.setEnabled(false);
        }

        download();

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept();
            }
        });

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decline();
            }
        });

        req_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImage();
            }
        });
    }

    public void download() {

        DatabaseReference clientReference = FirebaseDatabase.getInstance().getReference("Client");
        clientReference.child(getIntent().getStringExtra("req_client_ID"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Client client = dataSnapshot.getValue(Client.class);
                            String s = Objects.requireNonNull(client).getFamily_Name() + " " + client.getFirst_Name();
                            clName.setText(s);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });


        ordinanceReference = FirebaseDatabase.getInstance().getReference().child("Prescription")
                .child(getIntent().getStringExtra("req_ID"));
        ordinanceReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Medication").exists()) {
                    listView.setVisibility(View.VISIBLE);
                    for (DataSnapshot ds : dataSnapshot.child("Medication").getChildren()) {
                        medications.add(new Medication(ds.getKey(), ds.getValue(Integer.class)));
                        medicationAdapter.notifyDataSetChanged();
                    }
                } else {
                    req_img.setVisibility(View.VISIBLE);
                    StorageReference imageReference = FirebaseStorage.getInstance().getReference()
                            .child("Prescription").child(getIntent().getStringExtra("req_ID"));
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            req_img.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            req_img.setImageResource(R.drawable.no_image_downloaded);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void accept() {
        if (!TextUtils.isEmpty(total.getText().toString())) {
            ordinanceReference.child("state").setValue("1");
            ordinanceReference.child("total").setValue(total.getText().toString());
            total.setVisibility(View.INVISIBLE);
            final EditText editText = new EditText(ClientRequestActivity.this);
            AlertDialog dialog = new AlertDialog.Builder(ClientRequestActivity.this)
                    .setTitle(getString(R.string.set_note))
                    .setView(editText)
                    .setPositiveButton(getString(R.string.set_note), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!TextUtils.isEmpty(editText.getText().toString()))
                                ordinanceReference.child("client_Note").setValue(editText.getText().toString());
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
            total.setVisibility(View.GONE);
        } else {
            total.setError(getString(R.string.put_total));
        }
        if ((getIntent().getStringExtra("req_state").equals("3"))) {
            Intent intent = new Intent(ClientRequestActivity.this, DeliveryMenActivity.class);
            intent.putExtra("send", 1);
            startActivityForResult(intent, 1);
        }
    }

    public void decline() {
        if ((getIntent().getStringExtra("req_state").equals("0"))) {
            ordinanceReference.child("state").setValue("2");
            final EditText editText = new EditText(ClientRequestActivity.this);
            AlertDialog dialog = new AlertDialog.Builder(ClientRequestActivity.this)
                    .setTitle(getString(R.string.set_note))
                    .setView(editText)
                    .setPositiveButton(getString(R.string.set_note), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!TextUtils.isEmpty(editText.getText().toString())) {
                                ordinanceReference.child("client_Note").setValue(editText.getText().toString());
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

            total.setVisibility(View.GONE);
        } else if ((getIntent().getStringExtra("req_state").equals("3"))) {
            ordinanceReference.child("state").setValue("6");
        }
        accept.setEnabled(false);
        decline.setEnabled(false);
    }

    public void showImage() {
        if (req_img.getDrawable() != null && req_img.getDrawable() != getDrawable(R.drawable.no_image_downloaded)) {
            Bitmap bitmap = ((BitmapDrawable) req_img.getDrawable()).getBitmap();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),
                    bitmap, "Title", null);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(path), "image/*");
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            ordinanceReference.child("delivery_ID").setValue(Objects.requireNonNull(data)
                    .getStringExtra("Delivery Man"));
            ordinanceReference.child("state").setValue("5");
            decline.setEnabled(false);
            accept.setEnabled(false);
            final EditText editText = new EditText(ClientRequestActivity.this);
            AlertDialog dialog = new AlertDialog.Builder(ClientRequestActivity.this)
                    .setTitle(getString(R.string.set_note))
                    .setView(editText)
                    .setPositiveButton(getString(R.string.set_note), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!TextUtils.isEmpty(editText.getText().toString())) {
                                ordinanceReference.child("deliveryMan_Note").setValue(editText.getText().toString());
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
