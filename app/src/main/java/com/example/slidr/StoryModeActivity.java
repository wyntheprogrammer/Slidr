package com.example.slidr;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.PuzzleUnlock;
import com.example.slidr.database.UserProgress;
import com.example.slidr.models.StoryData;

import java.util.List;

public class StoryModeActivity extends AppCompatActivity {

    private AppDatabase database;
    private String storyId;
    private StoryData.StoryMode storyMode;
    private LinearLayout arcsContainer;
    private TextView starsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_mode);

        database = AppDatabase.getInstance(this);
        storyId = getIntent().getStringExtra("STORY_ID");
        storyMode = StoryData.getStoryMode(storyId);

        TextView titleText = findViewById(R.id.tvStoryTitle);
        starsText = findViewById(R.id.tvStarsDisplay);
        arcsContainer = findViewById(R.id.arcsContainer);
        Button backBtn = findViewById(R.id.btnBack);

        titleText.setText(storyMode.name);
        backBtn.setOnClickListener(v -> finish());

        loadArcs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTotalStars();
        loadArcs();
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

    private void loadArcs() {
        new Thread(() -> {
            List<PuzzleUnlock> unlocks = database.gameDao().getStoryModeProgress(storyId);

            runOnUiThread(() -> {
                arcsContainer.removeAllViews();

                for (int i = 0; i < storyMode.arcs.length; i++) {
                    StoryData.Arc arc = storyMode.arcs[i];
                    PuzzleUnlock unlock = unlocks.get(i);

                    addArcCard(arc, unlock, i);
                }
            });
        }).start();
    }

    private void addArcCard(StoryData.Arc arc, PuzzleUnlock unlock, int arcIndex) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundColor(0xFFF5F5F5);
        card.setPadding(20, 20, 20, 20);
        card.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 15);
        card.setLayoutParams(params);

        // Image preview
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(120, 120);
        imageParams.setMargins(0, 0, 20, 0);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(arc.imageResId);

        if (!unlock.isUnlocked()) {
            imageView.setAlpha(0.3f);
        }

        // Info container
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        infoLayout.setLayoutParams(infoParams);

        TextView arcName = new TextView(this);
        arcName.setText(arc.name);
        arcName.setTextSize(18);
        arcName.setTextColor(0xFF333333);
        // Incorrect line
        // arcName.setTextStyle(android.graphics.Typeface.BOLD);
        // Correct line:
        arcName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView statusText = new TextView(this);
        if (unlock.isUnlocked()) {
            String stars = "★".repeat(unlock.getStarsEarned()) + "☆".repeat(3 - unlock.getStarsEarned());
            statusText.setText(stars + " | " + unlock.getStarsEarned() + "/3 Stars");
            statusText.setTextColor(0xFF4CAF50);
        } else {
            statusText.setText("🔒 Requires " + arc.starsRequired + " stars");
            statusText.setTextColor(0xFF999999);
        }
        statusText.setTextSize(14);
        statusText.setPadding(0, 5, 0, 0);

        infoLayout.addView(arcName);
        infoLayout.addView(statusText);

        // Play button
        Button playBtn = new Button(this);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        playBtn.setLayoutParams(btnParams);
        playBtn.setText(unlock.isUnlocked() ? "Play" : "Locked");
        playBtn.setEnabled(unlock.isUnlocked());
        playBtn.setBackgroundColor(unlock.isUnlocked() ? storyMode.color : 0xFF999999);
        playBtn.setTextColor(Color.WHITE);

        playBtn.setOnClickListener(v -> {
            if (unlock.isUnlocked()) {
                Intent intent = new Intent(this, ArcDifficultyActivity.class);
                intent.putExtra("STORY_ID", storyId);
                intent.putExtra("ARC_INDEX", arcIndex);
                intent.putExtra("ARC_NAME", arc.name);
                intent.putExtra("IMAGE_RES_ID", arc.imageResId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Need " + arc.starsRequired + " stars to unlock", Toast.LENGTH_SHORT).show();
            }
        });

        card.addView(imageView);
        card.addView(infoLayout);
        card.addView(playBtn);

        arcsContainer.addView(card);
    }
}