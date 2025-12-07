package com.example.slidr;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.GameSettings;
import com.example.slidr.database.MusicTrack;
import com.example.slidr.database.User;
import com.example.slidr.database.UserProgress;
import com.example.slidr.utils.MusicManager;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private AppDatabase database;
    private Switch musicSwitch;
    private LinearLayout musicTracksContainer;
    private GameSettings settings;
    private TextView unlockHintText;
    private int userTotalStars = 0;
    private RadioGroup radioGroup;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        database = AppDatabase.getInstance(this);

        TextView titleText = findViewById(R.id.tvSettingsTitle);
        musicSwitch = findViewById(R.id.switchMusic);
        musicTracksContainer = findViewById(R.id.musicTracksContainer);
        unlockHintText = findViewById(R.id.tvUnlockHint);
        Button backBtn = findViewById(R.id.btnBack);

        loadCurrentUser();
        loadSettings();
        loadUserStars();
        loadMusicTracks();

        musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (settings == null) return;

            settings.setMusicEnabled(isChecked);
            saveSettings();

            // Enable/disable all music track rows
            for (int i = 0; i < musicTracksContainer.getChildCount(); i++) {
                View child = musicTracksContainer.getChildAt(i);
                child.setEnabled(isChecked);
                child.setAlpha(isChecked ? 1.0f : 0.5f);
            }

            if (!isChecked) {
                // Stop music when switch is turned off
                MusicManager.stopMusic();
            } else {
                // Resume music if a track is selected (not "No Music")
                if (settings.getSelectedMusicId() != -1) {
                    new Thread(() -> {
                        MusicTrack track = database.gameDao().getMusicTrack(settings.getSelectedMusicId());
                        if (track != null && track.isUnlocked()) {
                            runOnUiThread(() -> {
                                MusicManager.playMusic(this, track.getMusicResId());
                            });
                        }
                    }).start();
                }
            }
        });

        backBtn.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserStars();
        loadMusicTracks();
    }

    private void loadCurrentUser() {
        new Thread(() -> {
            currentUser = database.gameDao().getLoggedInUser();
        }).start();
    }

    private void loadSettings() {
        new Thread(() -> {
            settings = database.gameDao().getSettings(); // Gets logged-in user's settings
            if (settings == null && currentUser != null) {
                // Create default settings for this user
                settings = new GameSettings();
                settings.setUserId(currentUser.getId());
                settings.setMusicEnabled(false);
                settings.setSelectedMusicId(-1);
                database.gameDao().insertSettings(settings);
            }

            runOnUiThread(() -> {
                if (settings != null) {
                    musicSwitch.setChecked(settings.isMusicEnabled());
                }
            });
        }).start();
    }

    private void loadUserStars() {
        new Thread(() -> {
            UserProgress progress = database.gameDao().getUserProgress();
            userTotalStars = (progress != null) ? progress.getTotalStars() : 0;
        }).start();
    }

    private void loadMusicTracks() {
        new Thread(() -> {
            List<MusicTrack> allTracks = database.gameDao().getAllMusicTracks(); // Gets logged-in user's tracks
            UserProgress progress = database.gameDao().getUserProgress();
            int totalStars = (progress != null) ? progress.getTotalStars() : 0;

            runOnUiThread(() -> {
                musicTracksContainer.removeAllViews();

                // Update unlock hint
                if (totalStars < 2) {
                    unlockHintText.setText("🎵 Earn 2 stars to unlock music!\nCurrent stars: " + totalStars);
                    unlockHintText.setVisibility(TextView.VISIBLE);
                } else {
                    unlockHintText.setText("🎵 Complete story arcs to unlock more music!");
                    unlockHintText.setVisibility(TextView.VISIBLE);
                }

                // Add "No Music" option at the top
                addNoMusicRow();

                // Add separator
                View separator = new View(this);
                separator.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                ));
                separator.setBackgroundColor(0xFFE0E0E0);
                LinearLayout.LayoutParams sepParams = (LinearLayout.LayoutParams) separator.getLayoutParams();
                sepParams.setMargins(0, 10, 0, 10);
                separator.setLayoutParams(sepParams);
                musicTracksContainer.addView(separator);

                // Add music tracks in table format
                for (MusicTrack track : allTracks) {
                    addMusicTrackRow(track, totalStars);
                }

                // Enable/disable based on music switch
                boolean enabled = (settings != null) && settings.isMusicEnabled();
                for (int i = 0; i < musicTracksContainer.getChildCount(); i++) {
                    View child = musicTracksContainer.getChildAt(i);
                    child.setEnabled(enabled);
                    child.setAlpha(enabled ? 1.0f : 0.5f);
                }
            });
        }).start();
    }

    private void addNoMusicRow() {
        // Get theme colors
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue, true);
        int surfaceColor = typedValue.data;

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int onSurfaceColor = typedValue.data;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(20, 20, 20, 20);
        row.setBackgroundColor(surfaceColor);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, 5);
        row.setLayoutParams(rowParams);

        // Left side - Track name
        TextView trackName = new TextView(this);
        trackName.setText("🔇 No Music");
        trackName.setTextSize(16);
        trackName.setTextColor(onSurfaceColor);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        trackName.setLayoutParams(nameParams);

        // Right side - Radio button (should be checked by default)
        RadioButton radioButton = new RadioButton(this);
        radioButton.setChecked(settings != null && settings.getSelectedMusicId() == -1);
        radioButton.setOnClickListener(v -> {
            if (settings != null) {
                settings.setSelectedMusicId(-1);
                saveSettings();
                MusicManager.stopMusic();
                Toast.makeText(this, "Music disabled", Toast.LENGTH_SHORT).show();
                loadMusicTracks(); // Refresh to update radio buttons
            }
        });

        row.addView(trackName);
        row.addView(radioButton);
        musicTracksContainer.addView(row);
    }

    private void addMusicTrackRow(MusicTrack track, int totalStars) {
        // Get theme colors
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue, true);
        int surfaceColor = typedValue.data;

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int onSurfaceColor = typedValue.data;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(20, 20, 20, 20);
        row.setBackgroundColor(surfaceColor);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, 5);
        row.setLayoutParams(rowParams);

        // Left side - Track info
        LinearLayout leftContainer = new LinearLayout(this);
        leftContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        leftContainer.setLayoutParams(leftParams);

        TextView trackName = new TextView(this);
        String lockIcon = track.isUnlocked() ? "🎵" : "🔒";
        String storyEmoji = getStoryEmoji(track.getStoryMode());
        trackName.setText(String.format("%s %s %s", lockIcon, storyEmoji, track.getTrackName()));
        trackName.setTextSize(16);
        trackName.setTextColor(track.isUnlocked() ? onSurfaceColor : (onSurfaceColor & 0x80FFFFFF));

        TextView storyName = new TextView(this);
        storyName.setText(getStoryName(track.getStoryMode()));
        storyName.setTextSize(12);
        storyName.setTextColor(onSurfaceColor);
        storyName.setAlpha(0.6f);
        storyName.setPadding(0, 4, 0, 0);

        leftContainer.addView(trackName);
        leftContainer.addView(storyName);

        // Right side - Action buttons
        LinearLayout rightContainer = new LinearLayout(this);
        rightContainer.setOrientation(LinearLayout.HORIZONTAL);
        rightContainer.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rightContainer.setLayoutParams(rightParams);

        if (track.isUnlocked()) {
            // Show radio button for selection
            RadioButton radioButton = new RadioButton(this);
            radioButton.setChecked(settings != null && settings.getSelectedMusicId() == track.getId());
            radioButton.setOnClickListener(v -> {
                if (settings != null) {
                    settings.setSelectedMusicId(track.getId());
                    saveSettings();

                    if (settings.isMusicEnabled()) {
                        MusicManager.playMusic(this, track.getMusicResId());
                        Toast.makeText(this, "Now playing: " + track.getTrackName(), Toast.LENGTH_SHORT).show();
                    }
                    loadMusicTracks(); // Refresh to update radio buttons
                }
            });
            rightContainer.addView(radioButton);
        } else {
            // Show unlock button if user has enough stars
            if (totalStars >= 2) {
                Button unlockBtn = new Button(this);
                unlockBtn.setText("Unlock (2⭐)");
                unlockBtn.setTextSize(12);
                unlockBtn.setBackgroundColor(0xFF4CAF50);
                unlockBtn.setTextColor(0xFFFFFFFF);
                unlockBtn.setPadding(30, 15, 30, 15);
                unlockBtn.setOnClickListener(v -> showUnlockDialog(track));
                rightContainer.addView(unlockBtn);
            } else {
                // Show locked status
                TextView lockedText = new TextView(this);
                lockedText.setText("🔒 Locked");
                lockedText.setTextSize(14);
                lockedText.setTextColor(onSurfaceColor);
                lockedText.setAlpha(0.5f);
                lockedText.setPadding(20, 0, 0, 0);
                rightContainer.addView(lockedText);
            }
        }

        row.addView(leftContainer);
        row.addView(rightContainer);
        musicTracksContainer.addView(row);
    }

    private void showUnlockDialog(MusicTrack track) {
        String storyName = getStoryName(track.getStoryMode());

        new AlertDialog.Builder(this)
                .setTitle("Unlock Music Track")
                .setMessage(String.format("Unlock %s?\n\nFrom: %s\nCost: 2 ⭐\nYour Stars: %d ⭐\nRemaining: %d ⭐",
                        track.getTrackName(),
                        storyName,
                        userTotalStars,
                        userTotalStars - 2))
                .setPositiveButton("Unlock", (dialog, which) -> unlockMusic(track))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void unlockMusic(MusicTrack track) {
        new Thread(() -> {
            // Check if user still has enough stars
            UserProgress progress = database.gameDao().getUserProgress();
            if (progress == null || progress.getTotalStars() < 2) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Not enough stars!", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // Deduct stars
            progress.setTotalStars(progress.getTotalStars() - 2);
            database.gameDao().updateUserProgress(progress);

            // Unlock the track
            track.setUnlocked(true);
            database.gameDao().updateMusicTrack(track);

            runOnUiThread(() -> {
                Toast.makeText(this, "🎵 " + track.getTrackName() + " unlocked! (-2⭐)", Toast.LENGTH_LONG).show();
                loadUserStars();
                loadMusicTracks();
            });
        }).start();
    }

    private String getStoryEmoji(String storyMode) {
        switch (storyMode) {
            case "onepiece": return "🏴‍☠️";
            case "dragonball": return "🐉";
            case "bleach": return "⚔️";
            default: return "🎮";
        }
    }

    private String getStoryName(String storyMode) {
        switch (storyMode) {
            case "onepiece": return "One Piece";
            case "dragonball": return "Dragon Ball Z";
            case "bleach": return "Bleach";
            default: return "Classic Mode";
        }
    }

    private void saveSettings() {
        if (settings != null) {
            new Thread(() -> database.gameDao().updateSettings(settings)).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}