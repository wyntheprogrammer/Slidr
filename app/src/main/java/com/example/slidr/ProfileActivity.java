package com.example.slidr;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private AppDatabase database;
    private User currentUser;

    private TextView tvUsername, tvEmail, tvCreatedAt, tvLastLogin;
    private TextInputEditText etEditUsername, etEditEmail;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private Button btnSaveProfile, btnChangePassword, btnLogout, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        database = AppDatabase.getInstance(this);

        // Initialize views
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvLastLogin = findViewById(R.id.tvLastLogin);

        etEditUsername = findViewById(R.id.etEditUsername);
        etEditEmail = findViewById(R.id.etEditEmail);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        // Load user data
        loadUserData();

        // Set up button listeners
        btnSaveProfile.setOnClickListener(v -> handleSaveProfile());
        btnChangePassword.setOnClickListener(v -> handleChangePassword());
        btnLogout.setOnClickListener(v -> handleLogout());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        new Thread(() -> {
            currentUser = database.gameDao().getLoggedInUser();

            if (currentUser == null) {
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("No logged in user found")
                            .setPositiveButton("OK", (dialog, which) -> finish())
                            .show();
                });
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            runOnUiThread(() -> {
                tvUsername.setText(currentUser.getUsername());
                tvEmail.setText(currentUser.getEmail());
                tvCreatedAt.setText(dateFormat.format(new Date(currentUser.getCreatedAt())));
                tvLastLogin.setText(dateFormat.format(new Date(currentUser.getLastLoginAt())));

                etEditUsername.setText(currentUser.getUsername());
                etEditEmail.setText(currentUser.getEmail());
            });
        }).start();
    }

    private void handleSaveProfile() {
        String newUsername = etEditUsername.getText().toString().trim();
        String newEmail = etEditEmail.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(newUsername)) {
            etEditUsername.setError("Username is required");
            etEditUsername.requestFocus();
            return;
        }

        if (newUsername.length() < 3) {
            etEditUsername.setError("Username must be at least 3 characters");
            etEditUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newEmail)) {
            etEditEmail.setError("Email is required");
            etEditEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            etEditEmail.setError("Please enter a valid email");
            etEditEmail.requestFocus();
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        new Thread(() -> {
            try {
                // Check if username is taken by another user
                if (!newUsername.equals(currentUser.getUsername())) {
                    User existingUsername = database.gameDao().getUserByUsername(newUsername);
                    if (existingUsername != null) {
                        runOnUiThread(() -> {
                            etEditUsername.setError("Username already taken");
                            btnSaveProfile.setEnabled(true);
                            btnSaveProfile.setText("💾 Save Changes");
                        });
                        return;
                    }
                }

                // Check if email is taken by another user
                if (!newEmail.equals(currentUser.getEmail())) {
                    User existingEmail = database.gameDao().getUserByEmail(newEmail);
                    if (existingEmail != null) {
                        runOnUiThread(() -> {
                            etEditEmail.setError("Email already registered");
                            btnSaveProfile.setEnabled(true);
                            btnSaveProfile.setText("💾 Save Changes");
                        });
                        return;
                    }
                }

                // Update user
                currentUser.setUsername(newUsername);
                currentUser.setEmail(newEmail);
                database.gameDao().updateUser(currentUser);

                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Success")
                            .setMessage("Profile updated successfully!")
                            .setPositiveButton("OK", (dialog, which) -> loadUserData())
                            .show();

                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("💾 Save Changes");
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Failed to update profile: " + e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();

                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("💾 Save Changes");
                });
            }
        }).start();
    }

    private void handleChangePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmNewPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Current password is required");
            etCurrentPassword.requestFocus();
            return;
        }

        if (!currentPassword.equals(currentUser.getPassword())) {
            etCurrentPassword.setError("Current password is incorrect");
            etCurrentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("New password is required");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmNewPassword.setError("Passwords do not match");
            etConfirmNewPassword.requestFocus();
            return;
        }

        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Changing...");

        new Thread(() -> {
            try {
                currentUser.setPassword(newPassword);
                database.gameDao().updateUser(currentUser);

                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Success")
                            .setMessage("Password changed successfully!")
                            .setPositiveButton("OK", (dialog, which) -> {
                                etCurrentPassword.setText("");
                                etNewPassword.setText("");
                                etConfirmNewPassword.setText("");
                            })
                            .show();

                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("🔒 Change Password");
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Failed to change password: " + e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();

                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("🔒 Change Password");
                });
            }
        }).start();
    }

    private void handleLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    new Thread(() -> {
                        database.gameDao().logoutAllUsers();
                        runOnUiThread(() -> {
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                    }).start();
                })
                .setNegativeButton("No", null)
                .show();
    }
}