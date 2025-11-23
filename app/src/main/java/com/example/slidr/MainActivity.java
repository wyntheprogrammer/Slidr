package com.example.slidr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.GameSettings;
import com.example.slidr.database.User;
import com.example.slidr.database.UserProgress;
import com.example.slidr.utils.MusicManager;

public class MainActivity extends AppCompatActivity {

    private AppDatabase database;
    private TextView starsText;
    private FrameLayout storyModeFrame;
    private ImageButton musicButton, profileButton;
    private GameSettings settings;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getInstance(this);

        starsText = findViewById(R.id.tvTotalStars);
        storyModeFrame = findViewById(R.id.storyModeFrame);
        musicButton = findViewById(R.id.btnMusicFloat);
        profileButton = findViewById(R.id.btnProfileFloat);

        Button classicBtn = findViewById(R.id.btnClassic);
        Button storyModeBtn = findViewById(R.id.btnStoryMode);
        Button onePieceBtn = findViewById(R.id.btnOnePiece);
        Button dragonBallBtn = findViewById(R.id.btnDragonBall);
        Button bleachBtn = findViewById(R.id.btnBleach);
        Button backFromStoryBtn = findViewById(R.id.btnBackFromStory);
        ImageButton statsBtn = findViewById(R.id.btnStatsIcon);

        // Load user info
        loadUserInfo();
        updateMusicButton();

        // Classic mode
        classicBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, DifficultyActivity.class);
            intent.putExtra("MODE", "classic");
            startActivity(intent);
        });

        // Story mode - show selection frame
        storyModeBtn.setOnClickListener(v -> {
            storyModeFrame.setVisibility(View.VISIBLE);
        });

        // Back from story selection
        backFromStoryBtn.setOnClickListener(v -> {
            storyModeFrame.setVisibility(View.GONE);
        });

        // Story modes
        onePieceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, StoryModeActivity.class);
            intent.putExtra("STORY_ID", "onepiece");
            startActivity(intent);
            storyModeFrame.setVisibility(View.GONE);
        });

        dragonBallBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, StoryModeActivity.class);
            intent.putExtra("STORY_ID", "dragonball");
            startActivity(intent);
            storyModeFrame.setVisibility(View.GONE);
        });

        bleachBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, StoryModeActivity.class);
            intent.putExtra("STORY_ID", "bleach");
            startActivity(intent);
            storyModeFrame.setVisibility(View.GONE);
        });

        // Statistics
        statsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            startActivity(intent);
        });

        // Floating music button
        musicButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // Profile button
        profileButton.setOnClickListener(v -> {
            showProfileDialog();
        });

        // Close story frame when clicking outside
        storyModeFrame.setOnClickListener(v -> {
            storyModeFrame.setVisibility(View.GONE);
        });

        // Prevent clicks on dialog from closing it
        View dialogContainer = findViewById(R.id.storyModeFrame);
        if (dialogContainer != null) {
            if (dialogContainer instanceof FrameLayout) {
                FrameLayout frameLayout = (FrameLayout) dialogContainer;
                if (frameLayout.getChildCount() > 0) {
                    View innerLayout = frameLayout.getChildAt(0);
                    innerLayout.setOnClickListener(null);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTotalStars();
        loadUserInfo();
        updateMusicButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            MusicManager.stopMusic();
        }
    }

    private void loadUserInfo() {
        new Thread(() -> {
            currentUser = database.gameDao().getLoggedInUser();
            // Just load the user, no UI changes needed
        }).start();
    }

    private void loadTotalStars() {
        new Thread(() -> {
            UserProgress progress = database.gameDao().getUserProgress();
            if (progress != null) {
                runOnUiThread(() -> {
                    starsText.setText("⭐ " + progress.getTotalStars() + " Stars");
                });
            }
        }).start();
    }

    private void updateMusicButton() {
        new Thread(() -> {
            settings = database.gameDao().getSettings();
            if (settings != null) {
                runOnUiThread(() -> {
                    if (settings.isMusicEnabled()) {
                        musicButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                    } else {
                        musicButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
                    }
                });
            }
        }).start();
    }

    private void showProfileDialog() {
        if (currentUser == null) {
            // Guest mode - offer to login
            new AlertDialog.Builder(this)
                    .setTitle("Guest Mode")
                    .setMessage("You're playing as a guest. Create an account to save your progress!")
                    .setPositiveButton("Login", (dialog, which) -> {
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Continue as Guest", null)
                    .show();
        } else {
            // Show user profile
            String message = String.format(
                    "Username: %s\nEmail: %s\n\nAccount created: %s\nLast login: %s",
                    currentUser.getUsername(),
                    currentUser.getEmail(),
                    new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                            .format(new java.util.Date(currentUser.getCreatedAt())),
                    new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                            .format(new java.util.Date(currentUser.getLastLoginAt()))
            );

            new AlertDialog.Builder(this)
                    .setTitle("👤 Profile")
                    .setMessage(message)
                    .setPositiveButton("Logout", (dialog, which) -> handleLogout())
                    .setNegativeButton("Close", null)
                    .show();
        }
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
                            startActivity(intent);
                            finish();
                        });
                    }).start();
                })
                .setNegativeButton("No", null)
                .show();
    }
}