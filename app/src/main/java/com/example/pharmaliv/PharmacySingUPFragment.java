package com.example.pharmaliv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class PharmacySingUPFragment extends Fragment {

    private EditText editTextPhName, editTextPhLat, editTextPhLang, editTextEmail, editTextPassword, editTextConfirmPassword, editTextPhone;
    private Button buttonSingUP, buttonLocation;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mReference;
    private FirebaseUser mFirebaseUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pharmacy_sing_up, container, false);
        editTextPhName = view.findViewById(R.id.ph_name);
        editTextPhLat = view.findViewById(R.id.ph_lat);
        editTextPhLang = view.findViewById(R.id.ph_lang);
        buttonLocation = view.findViewById(R.id.ph_get_loc);
        editTextEmail = view.findViewById(R.id.ph_email);
        editTextPassword = view.findViewById(R.id.ph_password);
        editTextConfirmPassword = view.findViewById(R.id.ph_confirm_password);
        editTextPhone = view.findViewById(R.id.ph_phone);
        buttonSingUP = view.findViewById(R.id.ph_sing_up);
        initializeUI();
        buttonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        buttonSingUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editTextPhName.getText())) {
                    editTextPhName.setError(getString(R.string.no_name));
                    return;

                } else if (TextUtils.isEmpty(editTextPhLat.getText()) && TextUtils.isEmpty(editTextPhLang.getText())) {
                    editTextPhLat.setError(getString(R.string.no_lat));
                    editTextPhLang.setError(getString(R.string.no_lang));

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
                                    if (mFirebaseUser != null) {
                                        mReference.child("ph" + mFirebaseUser.getUid()).child("Name").setValue(editTextPhName.getText().toString());
                                        mReference.child("ph" + mFirebaseUser.getUid()).child("Latitude").setValue(editTextPhLat.getText().toString());
                                        mReference.child("ph" + mFirebaseUser.getUid()).child("Longitude").setValue(editTextPhLat.getText().toString());
                                        mReference.child("ph" + mFirebaseUser.getUid()).child("Phone").setValue(editTextPhone.getText().toString());
                                        mReference.child("ph" + mFirebaseUser.getUid()).child("Login ID").setValue(mFirebaseUser.getUid());
                                        Toast.makeText(getContext(), getString(R.string.sing_up_successful), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getContext(), MainActivity.class));
                                    }
                                } else {
                                    Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        buttonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getContext(), MapsActivity.class), 1);
            }
        });
        return view;
    }

    public void initializeUI() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference().child("Pharmacy");
        mProgressDialog = new ProgressDialog(getContext());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            editTextPhLat.setText(data.getStringExtra("latitude"));
            editTextPhLang.setText(data.getStringExtra("longitude"));
        }
    }
}
