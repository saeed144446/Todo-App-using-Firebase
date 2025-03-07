package com.example.userauth.Activies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.userauth.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileAct extends AppCompatActivity {

    private FirebaseAuth auth;
    FirebaseUser firebaseUser;
    Button editpassword, editEmail;
    ProgressDialog pd;
    private EditText newEmail;
    private  Button Logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editEmail = findViewById(R.id.update_email_btn);
        editpassword = findViewById(R.id.change_password_btn);
        ImageView backbtn = findViewById(R.id.backbtn);
        Logout = findViewById(R.id.logout_btn);

        pd = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Handle button clicks
        editEmail.setOnClickListener(v -> {
            pd.setMessage("Changing Email");
           showEmailChangeDialog();
        });
        editpassword.setOnClickListener(v -> {
            pd.setMessage("Changing Password");
            showPasswordChangeDialog();
        });
        Logout.setOnClickListener(v -> signOutAndStartSignInActivity());
    }

    // Handle email verification
    public void emailVeriftaion() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            // Send verification email
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Successfully sent the verification email
                                user.reload();  // Reload user info to ensure that the state is updated
                                Log.d("EmailVerification", "Verification email sent to: " + user.getEmail());
                                Toast.makeText(ProfileAct.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                            } else {
                                // Failed to send verification email
                                Log.e("EmailVerification", "Failed to send verification email: " + task.getException().getMessage());
                                Toast.makeText(ProfileAct.this, "Failed to send verification email.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            // Email is already verified or user is null
            if (user != null) {
                Log.d("EmailVerification", "Email already verified: " + user.getEmail());
            } else {
                Log.d("EmailVerification", "User is null, cannot send verification email.");
            }
        }
    }
    //show password change dialog
    private void showPasswordChangeDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_password, null);
        final EditText oldpass = view.findViewById(R.id.oldpasslog);
        final EditText newpass = view.findViewById(R.id.newpasslog);
        Button updateButton = view.findViewById(R.id.updatepass);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldp = oldpass.getText().toString().trim();
                String newp = newpass.getText().toString().trim();
                if (TextUtils.isEmpty(oldp)) {
                    Toast.makeText(ProfileAct.this, "Current Password can't be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(newp)) {
                    Toast.makeText(ProfileAct.this, "New Password can't be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                dialog.dismiss();
                updatePassword(oldp, newp);
            }
        });
    }
    // Handle password update
    private void updatePassword(String oldp, final String newp) {
        pd.show();
        final FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            pd.dismiss();
            Toast.makeText(ProfileAct.this, "User not logged in", Toast.LENGTH_LONG).show();
            return;
        }

        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldp);

        user.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    user.updatePassword(newp).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pd.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileAct.this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ProfileAct.this, "Failed to update password", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    pd.dismiss();
                    Toast.makeText(ProfileAct.this, "Re-authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    //show email change dialog
    private void showEmailChangeDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_email, null);
        final EditText currentPassword = view.findViewById(R.id.oldpasslog);
        newEmail = view.findViewById(R.id.newEmaillog);
        Button updateButton = view.findViewById(R.id.updateEmailButton);
        Button verificationButton = view.findViewById(R.id.verifitionEmail);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // Check if email is verified
            if (user.isEmailVerified()) {
                // If the email is already verified, disable the verification button
                verificationButton.setEnabled(false);
                verificationButton.setText("Email Verified");
                Toast.makeText(ProfileAct.this, "Your email is already verified.", Toast.LENGTH_LONG).show();
            } else {
                // If email is not verified, allow user to send the verification email
                verificationButton.setEnabled(true);
                verificationButton.setText("Send Verification Email");

                verificationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emailVeriftaion();  // Call the method to send the verification email
                    }
                });
            }
        }

        // Handle email update process
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = currentPassword.getText().toString().trim();
                String newEmailAddress = newEmail.getText().toString().trim();
                if (!user.isEmailVerified()) {
                    showEmailVerificationDialog();
                }
                else if (TextUtils.isEmpty(oldPassword)) {
                    Toast.makeText(ProfileAct.this, "Password can't be empty", Toast.LENGTH_LONG).show();
                    return;
                }else if (TextUtils.isEmpty(newEmailAddress)) {
                    Toast.makeText(ProfileAct.this, "New Email can't be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                dialog.dismiss();
                updateEmail(oldPassword, newEmailAddress);
            }
        });
    }
    // Handle email update
    private void updateEmail(String oldPassword, String newEmailAddress) {
        pd.show();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            pd.dismiss();
            showToast("User not logged in.");
            return;
        }

        // Ensure the email format is correct
        if (!isValidEmail(newEmailAddress)) {
            pd.dismiss();
            showToast("Invalid email format.");
            return;
        }

        // Check if email is verified
        if (!user.isEmailVerified()) {
            pd.dismiss();
           Log.d("EmailVerification", "Email is not verified, showing dialog.");
        showEmailVerificationDialog();
        return;
        }

        // Reauthenticate using the old password
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Proceed with email update if reauthentication is successful
                        user.verifyBeforeUpdateEmail(newEmailAddress)
                                .addOnCompleteListener(emailUpdateTask -> onEmailUpdateComplete(emailUpdateTask, user));
                    } else {
                        pd.dismiss();
                        // Handle reauthentication failure
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            showToast("Incorrect password. Please try again.");
                        } else if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                            showToast("Recent sign-in required. Please sign in again.");
                            signOutAndStartSignInActivity();
                        } else {
                            showToast("Authentication failed. Please check your password.");
                        }

                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                    }
                });
    }
    // Handle email update completion
    private void onEmailUpdateComplete(@NonNull Task<Void> task, FirebaseUser user) {
        pd.dismiss(); // Dismiss the dialog first

        if (task.isSuccessful()) {
            signOutAndStartSignInActivity();
            showToast(getString(R.string.email_updated_success));
            // Additional actions after successful update, e.g., sign out
        } else {
            showToast(getString(R.string.email_update_failed));
            // Handle the error, e.g., log it, show a more specific error message
            if (task.getException() != null) {
                task.getException().printStackTrace();
                // Log the error or show a more specific error message
                Log.e("Email Update Error", "Error updating email", task.getException());
            }
        }
    }
    private boolean isValidEmail(String email) {
        // Simple regex to check if the email is in a valid format
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    // Show a toast message
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
    // Sign out and start the sign-in activity
    private void signOutAndStartSignInActivity() {
        auth.signOut();
        // Start your sign-in activity here
        Intent intent = new Intent(this, LoginAct.class); // Replace SignInActivity with your actual sign-in activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the back stack
        startActivity(intent);
        finish();
    }
    // Email verification dialog
    private void showEmailVerificationDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_emailverification, null);

        Button okayButton = view.findViewById(R.id.ok_button);
        TextView emailtext = view.findViewById(R.id.emailtext);
        if (firebaseUser != null) {
            String emailusername = firebaseUser.getEmail();
            emailtext.setText(emailusername);
        } else {
            emailtext.setText("No user logged in");
        }

        // Debug log to confirm dialog creation
        Log.d("EmailVerification", "Creating email verification dialog.");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        // Debug log to confirm dialog is showing
        dialog.show();
        Log.d("EmailVerification", "Email verification dialog is now visible.");

        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
}}


