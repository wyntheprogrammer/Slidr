package com.example.slidr;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button registerBtn;
    private TextView loginLink;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        database = AppDatabase.getInstance(this);

        usernameInput = findViewById(R.id.etUsername);
        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);
        confirmPasswordInput = findViewById(R.id.etConfirmPassword);
        registerBtn = findViewById(R.id.btnRegister);
        loginLink = findViewById(R.id.tvLoginLink);

        registerBtn.setOnClickListener(v -> handleRegister());

        loginLink.setOnClickListener(v -> {
            finish();
        });
    }

    private void handleRegister() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }

        if (username.length() < 3) {
            usernameInput.setError("Username must be at least 3 characters");
            usernameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        registerBtn.setEnabled(false);
        registerBtn.setText("Creating Account...");

        new Thread(() -> {
            try {
                // Check if email already exists
                User existingEmail = database.gameDao().getUserByEmail(email);
                if (existingEmail != null) {
                    runOnUiThread(() -> {
                        emailInput.setError("Email already registered");
                        registerBtn.setEnabled(true);
                        registerBtn.setText("Register");
                    });
                    return;
                }

                // Check if username already exists
                User existingUsername = database.gameDao().getUserByUsername(username);
                if (existingUsername != null) {
                    runOnUiThread(() -> {
                        usernameInput.setError("Username already taken");
                        registerBtn.setEnabled(true);
                        registerBtn.setText("Register");
                    });
                    return;
                }

                // Create new user
                User newUser = new User(username, email, password);
                database.gameDao().insertUser(newUser);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Account created successfully! Please login.", Toast.LENGTH_LONG).show();
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    registerBtn.setEnabled(true);
                    registerBtn.setText("Register");
                });
            }
        }).start();
    }
}