package com.example.pharmaliv;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ClientSingUPFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ClientSingUPFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClientSingUPFragment extends Fragment {

    private EditText editTextFirstName, editTextFamilyName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextPhone;
    private Button buttonSingUP;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mReference;
    private FirebaseUser mFirebaseUser;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ClientSingUPFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClientSingUPFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClientSingUPFragment newInstance(String param1, String param2) {
        ClientSingUPFragment fragment = new ClientSingUPFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
                mFirebaseAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                mProgressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    mFirebaseUser = mFirebaseAuth.getCurrentUser();
                                    if (mFirebaseUser != null){
                                        mReference.child("cl"+mFirebaseUser.getUid()).child("First Name").setValue(editTextFirstName.getText().toString());
                                        mReference.child("cl"+mFirebaseUser.getUid()).child("Family Name").setValue(editTextFamilyName.getText().toString());
                                        mReference.child("cl"+mFirebaseUser.getUid()).child("Phone").setValue(editTextPhone.getText().toString());
                                        mReference.child("cl"+mFirebaseUser.getUid()).child("Login ID").setValue(mFirebaseUser.getUid());
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_client_sing_up, container, false);
        editTextFirstName = view.findViewById(R.id.cl_first_name);
        editTextFamilyName = view.findViewById(R.id.cl_family_name);
        editTextEmail = view.findViewById(R.id.cl_email);
        editTextPassword = view.findViewById(R.id.cl_password);
        editTextConfirmPassword = view.findViewById(R.id.cl_confirm_password);
        editTextPhone = view.findViewById(R.id.cl_phone);
        buttonSingUP = view.findViewById(R.id.cl_sing_up);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void initializeUI() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference().child("Client");
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
}
