package com.example.pharmaliv;

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

public class DeliverySingUPFragment extends Fragment {

    private EditText editTextFirstName, editTextFamilyName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextPhone;
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
        View view = inflater.inflate(R.layout.fragment_delivery_sing_up, container, false);
        editTextFirstName = view.findViewById(R.id.dl_first_name);
        editTextFamilyName = view.findViewById(R.id.dl_family_name);
        editTextEmail = view.findViewById(R.id.dl_email);
        editTextPassword = view.findViewById(R.id.dl_password);
        editTextConfirmPassword = view.findViewById(R.id.dl_confirm_password);
        editTextPhone = view.findViewById(R.id.dl_phone);
        Button buttonSingUP = view.findViewById(R.id.dl_sing_up);
        initializeUI();
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
                signUP();
            }
        });
        return view;
    }

    public void initializeUI() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference().child("Delivery Man");
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

    public void signUP() {
        mFirebaseAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressDialog.dismiss();
                        if (task.isSuccessful()) {
                            mFirebaseUser = mFirebaseAuth.getCurrentUser();
                            if (mFirebaseUser != null) {
                                DeliveryMan deliveryMan = new DeliveryMan(mFirebaseUser.getUid(),
                                        editTextFirstName.getText().toString(),
                                        editTextFamilyName.getText().toString(),
                                        editTextPhone.getText().toString(),
                                        "0");
                                mReference.child("dl" + mFirebaseUser.getUid()).setValue(deliveryMan);
                                Toast.makeText(getContext(), getString(R.string.sing_up_successful), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getContext(), ClientActivity.class));
                            }
                        } else {
                            Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
