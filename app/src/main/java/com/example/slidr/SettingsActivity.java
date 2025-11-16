package com.example.slidr;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.GameSettings;
import com.example.slidr.database.MusicTrack;
import com.example.slidr.utils.MusicManager;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private AppDatabase database;
    private Switch musicSwitch;
    private RadioGroup musicRadioGroup;
    private GameSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        database = AppDatabase.getInstance(this);

        TextView titleText = findViewById(R.id.tvSettingsTitle);
        musicSwitch = findViewById(R.id.switchMusic);
        musicRadioGroup = findViewById(R.id.radioGroupMusic);
        Button backBtn = findViewById(R.id.btnBack);

        loadSettings();
        loadMusicTracks();

        musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.setMusicEnabled(isChecked);
            saveSettings();
            musicRadioGroup.setEnabled(isChecked);
            for (int i = 0; i < musicRadioGroup.getChildCount(); i++) {
                musicRadioGroup.getChildAt(i).setEnabled(isChecked);
            }

            // NEW: Handle music toggle
            if (!isChecked) {
                MusicManager.stopMusic();
            } else {
                // When turning music ON, start playing selected track
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
        // Reload tracks to refresh unlock status
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

    private void loadMusicTracks() {
        new Thread(() -> {
            List<MusicTrack> allTracks = database.gameDao().getAllMusicTracks();

            runOnUiThread(() -> {
                musicRadioGroup.removeAllViews();

                // Add "No Music" option
                RadioButton noMusicBtn = new RadioButton(this);
                noMusicBtn.setText("🔇 No Music");
                noMusicBtn.setId(-1);
                noMusicBtn.setTextSize(16);
                noMusicBtn.setPadding(20, 20, 20, 20);
                musicRadioGroup.addView(noMusicBtn);

                if (settings.getSelectedMusicId() == -1) {
                    noMusicBtn.setChecked(true);
                }

                // Add music tracks
                for (MusicTrack track : allTracks) {
                    RadioButton radioButton = new RadioButton(this);

                    String lockIcon = track.isUnlocked() ? "🎵" : "🔒";
                    String storyEmoji = getStoryEmoji(track.getStoryMode());
                    radioButton.setText(String.format("%s %s %s", lockIcon, storyEmoji, track.getTrackName()));

                    radioButton.setId(track.getId());
                    radioButton.setEnabled(track.isUnlocked() && settings.isMusicEnabled());
                    radioButton.setClickable(track.isUnlocked()); // NEW: Prevent clicking locked tracks
                    radioButton.setTextSize(16);
                    radioButton.setPadding(20, 20, 20, 20);

                    if (settings.getSelectedMusicId() == track.getId()) {
                        radioButton.setChecked(true);
                    }

                    musicRadioGroup.addView(radioButton);
                }

                musicRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    settings.setSelectedMusicId(checkedId);
                    saveSettings();

                    if (checkedId != -1) {
                        // NEW: Preview the selected music
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

    private String getStoryEmoji(String storyMode) {
        switch (storyMode) {
            case "onepiece": return "🏴‍☠️";
            case "dragonball": return "🐉";
            case "bleach": return "⚔️";
            default: return "🎮";
        }
    }

    private void saveSettings() {
        new Thread(() -> database.gameDao().updateSettings(settings)).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't stop music here - let it continue to other activities
    }
}