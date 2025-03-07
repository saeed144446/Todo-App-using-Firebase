package com.example.userauth.Activies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.userauth.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginAct extends AppCompatActivity {

    private FirebaseAuth auth;
    FirebaseUser firebaseUser;
    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText;
    private Button loginButton;
    private ImageView backbtn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        backbtn=findViewById(R.id.backbtn);
        signupRedirectText = findViewById(R.id.signUpRedirectText);
        // Initialize Firebase Auth instance
        auth = FirebaseAuth.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // Check if user is already logged in
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                String email = loginEmail.getText().toString().trim();
                String pass = loginPassword.getText().toString().trim();

                if (email.isEmpty()) {
                    loginEmail.setError("Email cannot be empty");
                    progressDialog.dismiss();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    loginEmail.setError("Please enter a valid email");
                } else if (pass.isEmpty()) {
                    progressDialog.dismiss();
                    loginPassword.setError("Password cannot be empty");
                } else {
                    auth.signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(authResult -> {

                                progressDialog.dismiss();
                                Toast.makeText(LoginAct.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginAct.this, MainActivity.class));
                                finish();
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginAct.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        // Set up Success and Failure Listeners
        signupRedirectText.setOnClickListener(v ->{

                startActivity(new Intent(LoginAct.this, SignUpAct.class));
        });
    }


}