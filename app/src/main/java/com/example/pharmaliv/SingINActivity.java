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

public class SingINActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonSingIN, buttonSingUP;
    private TextView textViewForgotPassword;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener stateListener;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);
        initializeUI();

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
                mFirebaseAuth.signInWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    mProgressDialog.dismiss();
                                    mFirebaseUser = mFirebaseAuth.getCurrentUser();
                                    Toast.makeText(getApplicationContext(), getString(R.string.sing_in_successful),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        buttonSingUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SingUPActivity.class));
            }
        });

        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SingINActivity.this, ForgotPasswordFragment.class));
            }
        });

        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("Client").hasChild("cl" + mFirebaseUser.getUid())) {
                                Intent intent = new Intent(SingINActivity.this, ClientActivity.class);
                                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                            } else if (dataSnapshot.child("Pharmacy").hasChild("ph" + mFirebaseUser.getUid())) {
                                Intent intent = new Intent(SingINActivity.this, PharmacyActivity.class);
                                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                            } else if (dataSnapshot.child("Delivery Man").hasChild("dl" + mFirebaseUser.getUid())) {
                                Intent intent = new Intent(SingINActivity.this, ClientActivity.class);
                                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(stateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(stateListener);
    }

    private void initializeUI() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.user_email);
        editTextPassword = findViewById(R.id.password);
        textViewForgotPassword = findViewById(R.id.forgot_password);
        buttonSingIN = findViewById(R.id.sign_in);
        buttonSingUP = findViewById(R.id.sing_up);
        mProgressDialog = new ProgressDialog(this);
    }

    private boolean isValidEmail() {
        return Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText()).matches();
    }
}
