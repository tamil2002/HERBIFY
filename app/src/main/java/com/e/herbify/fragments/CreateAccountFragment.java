package com.e.herbify.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.e.herbify.FragmentReplaceActivity;
import com.e.herbify.MainActivity;
import com.e.herbify.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountFragment extends Fragment {


    private EditText nameET, emailET, passwordET, confirmPasswordET;
    private ProgressBar progressBar;
    private TextView loginTV;
    private Button signUpBtn;
    private FirebaseAuth auth;

    public static final String EMAIL_REGEX = "^(.+)@(.+)$";


    public CreateAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        clickListeners();
    }

    private void init(View view) {
        nameET = view.findViewById(R.id.nameET);
        emailET = view.findViewById(R.id.emailET);
        passwordET = view.findViewById(R.id.passwordET);
        confirmPasswordET = view.findViewById(R.id.confirmPassET);
        loginTV = view.findViewById(R.id.loginTV);
        signUpBtn = view.findViewById(R.id.signUpBtn);
        progressBar = view.findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
    }

    private void clickListeners() {
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentReplaceActivity) getActivity()).setFragment(new LoginFragment());
            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameET.getText().toString();
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();
                String confirmPassword = confirmPasswordET.getText().toString();
                if(name.isEmpty() || name.equals("")){
                    nameET.setError("Please Input Name");
                    return;
                }
                if(email.isEmpty() || !email.matches(EMAIL_REGEX)){
                    emailET.setError("Please Input Valid email");
                    return;
                }
                if(password.isEmpty() || password.length() < 6){
                    passwordET.setError("Please Input Valid Password");
                    return;
                }
                if (!password.equals(confirmPassword)){
                    confirmPasswordET.setError("Password does not match");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);


                // Perform your signup action here
                createAccount(name,email,password);
            }
        });
    }
    private void createAccount(String name, String email, String password){
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                   if(task.isSuccessful()){
                       FirebaseUser user = auth.getCurrentUser();
                       user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful()){
                                   Toast.makeText(getContext(), "Verification Email Sent", Toast.LENGTH_SHORT).show();
                               }
                           }
                       });
                       uploadUser(user,name,email);


                   }else{
                       progressBar.setVisibility(View.GONE);

                       String exception = task.getException().getMessage();
                       Toast.makeText(getContext(), "Error :" + exception, Toast.LENGTH_SHORT).show();
                   }



                    }
                });

    }

    private void uploadUser(FirebaseUser user, String name, String email){
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        map.put("email",email);
        map.put("ProfileImage"," ");
        map.put("uid",user.getUid());

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                      assert getActivity() != null;
                      progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class ));
                    getActivity().finish();
                }else{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error :" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

}