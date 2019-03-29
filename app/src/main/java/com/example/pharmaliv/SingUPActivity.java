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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SingUPActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextFamilyName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextPhone;
    private Button buttonSingIN, buttonSingUP;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mReference;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);
        initializeUI();

        buttonSingIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttonSingUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((TextUtils.isEmpty(editTextFirstName.getText())) || (TextUtils.isEmpty(editTextFamilyName.getText()))) {
                    editTextFirstName.setError(getString(R.string.no_first_name));
                    editTextFamilyName.setError(getString(R.string.no_family_name));
                    return;
                } else if ((!isValidEmail()) || (TextUtils.isEmpty(editTextEmail.getText()))) {
                    editTextEmail.setError(getString(R.string.error_invalid_email));
                    return;
                } else if (TextUtils.isEmpty(editTextPassword.getText())) {
                    editTextPassword.setError(getString(R.string.error_invalid_password));
                    return;
                } else if ((!confirmedPassword()) || (TextUtils.isEmpty(editTextConfirmPassword.getText()))) {
                    editTextConfirmPassword.setError(getString(R.string.password_not_confirmed));
                    return;
                } else if ((!isValidPhone()) || (TextUtils.isEmpty(editTextPhone.getText()))) {
                    editTextPhone.setError(getString(R.string.error_invalid_phone));
                    return;
                }
                mProgressDialog.setMessage(getString(R.string.singing_up));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.show();
                mFirebaseAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                mProgressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    mFirebaseUser = mFirebaseAuth.getCurrentUser();
                                    mReference.child("First Name").setValue(editTextFirstName.getText().toString());
                                    mReference.child("Family Name").setValue(editTextFamilyName.getText().toString());
                                    mReference.child("Phone").setValue(editTextPhone.getText().toString());
                                    mReference.child("Login ID").setValue(mFirebaseUser.getUid());
                                    Toast.makeText(getApplicationContext(), getString(R.string.sing_up_successful), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                } else {
                                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    public void initializeUI() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference().child("Client").push();

        editTextFirstName = findViewById(R.id.firstname);
        editTextFamilyName = findViewById(R.id.familyname);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.confirmpasssword);
        editTextPhone = findViewById(R.id.phone);
        buttonSingIN = findViewById(R.id.sing_in);
        buttonSingUP = findViewById(R.id.sing_up);
        mProgressDialog = new ProgressDialog(this);
    }

    public boolean isValidEmail() {
        return Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText()).matches();
    }

    public boolean confirmedPassword() {
        return editTextPassword.getText().toString().equals(editTextConfirmPassword.getText().toString());
    }

    public boolean isValidPhone() {
        return Patterns.PHONE.matcher(editTextPhone.getText()).matches();
    }
}
