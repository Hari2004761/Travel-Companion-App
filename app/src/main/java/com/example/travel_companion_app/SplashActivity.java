package com.example.travel_companion_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            goToMain();
//            return;
//        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        Button btnSignIn = findViewById(R.id.btn_email_sign_in);
        Button btnRegister = findViewById(R.id.btn_register);
        Button btnGoogle = findViewById(R.id.signin_button);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);

        btnSignIn.setOnClickListener(v -> handleEmailSignIn());
        btnRegister.setOnClickListener(v -> handleRegister());
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        btnGoogle.setOnClickListener(v -> launchGoogleSignIn());
    }

    private void handleEmailSignIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        goToMain();
                    } else {
                        Toast.makeText(this,
                                "Sign-in failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleRegister() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        goToMain();
                    } else {
                        Toast.makeText(this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email address first", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset email sent to " + email,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void launchGoogleSignIn() {
        // Sign out first so the account picker always appears
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                try {
                    GoogleSignInAccount account = GoogleSignIn
                            .getSignedInAccountFromIntent(data)
                            .getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        goToMain();
                    } else {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToMain() {
        Intent intent = new Intent(this, ProfileSetupActivity.class);
        startActivity(intent);
        finish();
    }
}
