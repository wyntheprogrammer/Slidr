package com.example.slidr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ArcDifficultyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arc_difficulty);

        String storyId = getIntent().getStringExtra("STORY_ID");
        int arcIndex = getIntent().getIntExtra("ARC_INDEX", 0);
        String arcName = getIntent().getStringExtra("ARC_NAME");
        int imageResId = getIntent().getIntExtra("IMAGE_RES_ID", 0);

        TextView titleText = findViewById(R.id.tvArcName);
        ImageView previewImage = findViewById(R.id.ivArcPreview);
        Button easyBtn = findViewById(R.id.btnEasy);
        Button mediumBtn = findViewById(R.id.btnMedium);
        Button hardBtn = findViewById(R.id.btnHard);
        Button backBtn = findViewById(R.id.btnBack);

        titleText.setText(arcName);
        previewImage.setImageResource(imageResId);

        easyBtn.setOnClickListener(v -> startGame(storyId, arcIndex, arcName, imageResId, "easy", 1));
        mediumBtn.setOnClickListener(v -> startGame(storyId, arcIndex, arcName, imageResId, "medium", 2));
        hardBtn.setOnClickListener(v -> startGame(storyId, arcIndex, arcName, imageResId, "hard", 3));
        backBtn.setOnClickListener(v -> finish());
    }

    private void startGame(String storyId, int arcIndex, String arcName, int imageResId,
                           String difficulty, int starsToEarn) {
        // Set grid size based on difficulty
        int gridSize;
        switch (difficulty) {
            case "easy":
                gridSize = 3;  // 3x3
                break;
            case "medium":
                gridSize = 4;  // 4x4
                break;
            case "hard":
                gridSize = 5;  // 5x5
                break;
            default:
                gridSize = 4;
        }

        Intent intent = new Intent(this, ImageGameActivity.class);
        intent.putExtra("STORY_ID", storyId);
        intent.putExtra("ARC_INDEX", arcIndex);
        intent.putExtra("ARC_NAME", arcName);
        intent.putExtra("IMAGE_RES_ID", imageResId);
        intent.putExtra("DIFFICULTY", difficulty);
        intent.putExtra("STARS_TO_EARN", starsToEarn);
        intent.putExtra("GRID_SIZE", gridSize);  // NEW: Pass grid size
        startActivity(intent);
        finish(); // Close difficulty screen
    }
}