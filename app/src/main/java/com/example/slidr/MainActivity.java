package com.example.slidr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.UserProgress;
import com.example.slidr.utils.MusicManager;

public class MainActivity extends AppCompatActivity {

    private AppDatabase database;
    private TextView starsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getInstance(this);

        starsText = findViewById(R.id.tvTotalStars);
        Button classicBtn = findViewById(R.id.btnClassic);
        Button onePieceBtn = findViewById(R.id.btnOnePiece);
        Button dragonBallBtn = findViewById(R.id.btnDragonBall);
        Button bleachBtn = findViewById(R.id.btnBleach);
        Button statsBtn = findViewById(R.id.btnViewStats);
        Button settingsBtn = findViewById(R.id.btnSettings);

        // Classic number puzzle mode
        classicBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, DifficultyActivity.class);
            intent.putExtra("MODE", "classic");
            startActivity(intent);
        });

        // Story modes
        onePieceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, StoryModeActivity.class);
            intent.putExtra("STORY_ID", "onepiece");
            startActivity(intent);
        });

        dragonBallBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, StoryModeActivity.class);
            intent.putExtra("STORY_ID", "dragonball");
            startActivity(intent);
        });

        bleachBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, StoryModeActivity.class);
            intent.putExtra("STORY_ID", "bleach");
            startActivity(intent);
        });

        statsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            startActivity(intent);
        });

        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTotalStars();
        // Don't auto-start music on main menu, let user control via settings
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop music when app is fully closed
        if (isFinishing()) {
            MusicManager.stopMusic();
        }
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
}