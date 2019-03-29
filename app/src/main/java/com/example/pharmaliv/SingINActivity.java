package com.example.pharmaliv;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SingINActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonSingIN, buttonSingUP;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);
        initializeUI();

        buttonSingIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((!isValidEmail())||(TextUtils.isEmpty(editTextEmail.getText()))) {
                    editTextEmail.setError(getString(R.string.error_invalid_email));
                    return;
                }
                if (TextUtils.isEmpty(editTextPassword.getText())){
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
                                    Toast.makeText(getApplicationContext(), getString(R.string.sing_in_successful), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                } else {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
       // if (mFirebaseUser != null)
         //   startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
       // if (mFirebaseUser != null)
         //   startActivity(new Intent(this, MainActivity.class));
    }

    private void initializeUI() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonSingIN = findViewById(R.id.sign_in);
        buttonSingUP = findViewById(R.id.sing_up);
        mProgressDialog = new ProgressDialog(this);
    }

    private boolean isValidEmail() {
        return Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText()).matches();
    }
}
