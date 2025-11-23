package com.example.slidr;

import android.os.Bundle;
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
import com.example.slidr.database.UserProgress;
import com.example.slidr.utils.MusicManager;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private AppDatabase database;
    private Switch musicSwitch;
    private RadioGroup musicRadioGroup;
    private GameSettings settings;
    private TextView unlockHintText;
    private int userTotalStars = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        database = AppDatabase.getInstance(this);

        TextView titleText = findViewById(R.id.tvSettingsTitle);
        musicSwitch = findViewById(R.id.switchMusic);
        musicRadioGroup = findViewById(R.id.radioGroupMusic);
        unlockHintText = findViewById(R.id.tvUnlockHint);
        Button backBtn = findViewById(R.id.btnBack);

        loadSettings();
        loadUserStars();
        loadMusicTracks();

        musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.setMusicEnabled(isChecked);
            saveSettings();
            musicRadioGroup.setEnabled(isChecked);
            for (int i = 0; i < musicRadioGroup.getChildCount(); i++) {
                View child = musicRadioGroup.getChildAt(i);
                if (child instanceof RadioButton) {
                    child.setEnabled(isChecked);
                }
            }

            if (!isChecked) {
                MusicManager.stopMusic();
            } else {
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

    private void loadSettings() {
        new Thread(() -> {
            settings = database.gameDao().getSettings();
            if (settings == null) {
                settings = new GameSettings();
                database.gameDao().insertSettings(settings);
            }

            runOnUiThread(() -> {
                musicSwitch.setChecked(settings.isMusicEnabled());
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
            List<MusicTrack> allTracks = database.gameDao().getAllMusicTracks();
            UserProgress progress = database.gameDao().getUserProgress();
            int totalStars = (progress != null) ? progress.getTotalStars() : 0;

            runOnUiThread(() -> {
                musicRadioGroup.removeAllViews();

                // Add "No Music" option
                RadioButton noMusicBtn = new RadioButton(this);
                noMusicBtn.setText("🔇 No Music");
                noMusicBtn.setId(-1);
                noMusicBtn.setTextSize(16);
                noMusicBtn.setPadding(20, 20, 20, 20);
                noMusicBtn.setEnabled(settings.isMusicEnabled());
                musicRadioGroup.addView(noMusicBtn);

                if (settings.getSelectedMusicId() == -1) {
                    noMusicBtn.setChecked(true);
                }

                // Update unlock hint
                if (totalStars < 2) {
                    unlockHintText.setText("🎵 Earn 2 stars to unlock music!\nCurrent stars: " + totalStars);
                    unlockHintText.setVisibility(TextView.VISIBLE);
                } else {
                    unlockHintText.setText("🎵 Complete story arcs to unlock more music!");
                    unlockHintText.setVisibility(TextView.VISIBLE);
                }

                // Add music tracks
                for (MusicTrack track : allTracks) {
                    // Create a container for radio button + unlock button
                    LinearLayout trackLayout = new LinearLayout(this);
                    trackLayout.setOrientation(LinearLayout.HORIZONTAL);
                    trackLayout.setPadding(10, 10, 10, 10);

                    RadioButton radioButton = new RadioButton(this);
                    String lockIcon = track.isUnlocked() ? "🎵" : "🔒";
                    String storyEmoji = getStoryEmoji(track.getStoryMode());
                    String displayText = String.format("%s %s %s", lockIcon, storyEmoji, track.getTrackName());

                    radioButton.setText(displayText);
                    radioButton.setId(track.getId());
                    radioButton.setEnabled(track.isUnlocked() && settings.isMusicEnabled());
                    radioButton.setClickable(track.isUnlocked());
                    radioButton.setTextSize(14);
                    radioButton.setPadding(10, 15, 10, 15);

                    LinearLayout.LayoutParams radioParams = new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                    );
                    radioButton.setLayoutParams(radioParams);

                    if (settings.getSelectedMusicId() == track.getId()) {
                        radioButton.setChecked(true);
                    }

                    trackLayout.addView(radioButton);

                    // Add unlock button if track is locked and user has 2+ stars
                    if (!track.isUnlocked() && totalStars >= 2) {
                        Button unlockBtn = new Button(this);
                        unlockBtn.setText("Unlock (2⭐)");
                        unlockBtn.setTextSize(12);
                        unlockBtn.setBackgroundColor(0xFF4CAF50);
                        unlockBtn.setTextColor(0xFFFFFFFF);
                        unlockBtn.setPadding(20, 10, 20, 10);

                        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        btnParams.setMargins(10, 0, 0, 0);
                        unlockBtn.setLayoutParams(btnParams);

                        final MusicTrack finalTrack = track;
                        unlockBtn.setOnClickListener(v -> showUnlockDialog(finalTrack));

                        trackLayout.addView(unlockBtn);
                    }

                    musicRadioGroup.addView(trackLayout);
                }

                musicRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    settings.setSelectedMusicId(checkedId);
                    saveSettings();

                    if (checkedId != -1) {
                        new Thread(() -> {
                            MusicTrack selected = database.gameDao().getMusicTrack(checkedId);
                            if (selected != null) {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Now playing: " + selected.getTrackName(), Toast.LENGTH_SHORT).show();
                                    if (settings.isMusicEnabled()) {
                                        MusicManager.playMusic(this, selected.getMusicResId());
                                    }
                                });
                            }
                        }).start();
                    } else {
                        Toast.makeText(this, "Music disabled", Toast.LENGTH_SHORT).show();
                        MusicManager.stopMusic();
                    }
                });
            });
        }).start();
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
        new Thread(() -> database.gameDao().updateSettings(settings)).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}