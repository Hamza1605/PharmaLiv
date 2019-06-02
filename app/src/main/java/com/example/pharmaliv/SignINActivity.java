package com.example.pharmaliv;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignINActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private TextView textViewSingUP;
    private Button buttonSingIN;
    private TextView textViewForgotPassword;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener stateListener;
    private FirebaseUser user;
    private int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);

        initializeUI();

        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = auth.getCurrentUser();
                if (user != null) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("Client").hasChild("cl" + user.getUid())) {
                                startActivity(new Intent(SignINActivity.this, ClientActivity.class));
                                state = 1;
                            } else if (dataSnapshot.child("Pharmacy").hasChild("ph" + user.getUid())) {
                                startActivity(new Intent(SignINActivity.this, PharmacyActivity.class));
                                state = 1;
                            } else if (dataSnapshot.child("Delivery Man").hasChild("dl" + user.getUid())) {
                                startActivity(new Intent(SignINActivity.this, DeliveryManActivity.class));
                                state = 1;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        };

        buttonSingIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((!isValidEmail()) || (TextUtils.isEmpty(editTextEmail.getText()))) {
                    editTextEmail.setError(getString(R.string.error_invalid_email));
                    return;
                } else if (TextUtils.isEmpty(editTextPassword.getText())) {
                    editTextPassword.setError(getString(R.string.error_invalid_password));
                    return;
                }
                mProgressDialog.setMessage(getString(R.string.logging_in));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();
                auth.signInWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                mProgressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.sing_in_successful),
                                            Toast.LENGTH_SHORT).show();
                                    auth.addAuthStateListener(stateListener);
                                } else {
                                    Toast.makeText(SignINActivity.this,
                                            Objects.requireNonNull(task.getException()).getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        textViewSingUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUPActivity.class));

            }
        });

        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignINActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        auth.removeAuthStateListener(stateListener);
    }

    private void initializeUI() {
        auth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.ph_email);
        editTextPassword = findViewById(R.id.password);
        textViewForgotPassword = findViewById(R.id.forgot_password);
        buttonSingIN = findViewById(R.id.sign_in);
        textViewSingUP = findViewById(R.id.sign_up);
        mProgressDialog = new ProgressDialog(this);
    }

    private boolean isValidEmail() {
        return Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText()).matches();
    }
}
