package com.example.slidr;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.User;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
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
            Toast.makeText(this, "Guest Mode - Progress won't be saved", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
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

                        Toast.makeText(this, "Welcome back, " + user.getUsername() + "!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Login");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Login");
                });
            }
        }).start();
    }
}