package com.example.pharmaliv;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


public class ForgotPasswordFragment extends Fragment {

    private TextView email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        email = view.findViewById(R.id.email);
        Button send = view.findViewById(R.id.send);
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((!isValidEmail()) || (TextUtils.isEmpty(email.getText()))) {
                    email.setError(getString(R.string.error_invalid_email));
                    return;
                }
                auth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), R.string.email_sent, Toast.LENGTH_SHORT).show();
                            Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                                    .beginTransaction().remove(ForgotPasswordFragment.this).commit();
                        } else {
                            Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return view;
    }

    private boolean isValidEmail() {
        return Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches();
    }
}
