package com.example.slidr;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.User;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private Button loginBtn;
    private TextView registerLink, skipLogin;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        database = AppDatabase.getInstance(this);

        // Check if user is already logged in
        checkAutoLogin();

        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.btnLogin);
        registerLink = findViewById(R.id.tvRegisterLink);
        skipLogin = findViewById(R.id.tvSkipLogin);

        loginBtn.setOnClickListener(v -> handleLogin());

        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        skipLogin.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Guest Mode")
                    .setMessage("Continue as guest? Your progress won't be saved across devices.")
                    .setPositiveButton("Continue", (dialog, which) -> {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void checkAutoLogin() {
        new Thread(() -> {
            User loggedInUser = database.gameDao().getLoggedInUser();
            if (loggedInUser != null) {
                runOnUiThread(() -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
            }
        }).start();
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");

        new Thread(() -> {
            try {
                User user = database.gameDao().login(email, password);

                runOnUiThread(() -> {
                    if (user != null) {
                        // Update login status
                        database.gameDao().logoutAllUsers();
                        user.setLoggedIn(true);
                        user.setLastLoginAt(System.currentTimeMillis());
                        database.gameDao().updateUser(user);

                        // Initialize user-specific data
                        AppDatabase.initializeUserData(database, user.getId());

                        // Show welcome message
                        new AlertDialog.Builder(this)
                                .setTitle("Welcome!")
                                .setMessage("Welcome back, " + user.getUsername() + "!")
                                .setPositiveButton("Continue", (dialog, which) -> {
                                    Intent intent = new Intent(this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .show();
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("Login Failed")
                                .setMessage("Invalid email or password. Please try again.")
                                .setPositiveButton("OK", null)
                                .show();
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Login");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Login failed: " + e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Login");
                });
            }
        }).start();
    }
}