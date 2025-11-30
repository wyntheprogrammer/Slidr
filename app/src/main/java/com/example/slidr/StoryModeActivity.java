package com.example.slidr;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private LinearLayout storiesContainer;
    private TextView starsText;
    private int currentTotalStars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_mode);

        database = AppDatabase.getInstance(this);
        storyId = getIntent().getStringExtra("STORY_ID");
        storyMode = StoryData.getStoryMode(storyId);

        TextView titleText = findViewById(R.id.tvStoryTitle);
        starsText = findViewById(R.id.tvStarsDisplay);
        storiesContainer = findViewById(R.id.storiesContainer);
        Button backBtn = findViewById(R.id.btnBack);

        titleText.setText(storyMode.name);
        backBtn.setOnClickListener(v -> finish());

        loadStories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTotalStars();
        loadStories();
    }

    private void loadTotalStars() {
        new Thread(() -> {
            UserProgress progress = database.gameDao().getUserProgress();
            if (progress != null) {
                currentTotalStars = progress.getTotalStars();
                runOnUiThread(() -> {
                    starsText.setText("⭐ " + currentTotalStars + " Stars");
                });
            }
        }).start();
    }

    private void loadStories() {
        new Thread(() -> {
            List<PuzzleUnlock> unlocks = database.gameDao().getStoryModeProgress(storyId);

            runOnUiThread(() -> {
                storiesContainer.removeAllViews();

                for (int i = 0; i < storyMode.stories.length; i++) {
                    StoryData.Story story = storyMode.stories[i];
                    PuzzleUnlock unlock = unlocks.get(i);

                    addStoryCard(story, unlock, i);
                }
            });
        }).start();
    }

    private void addStoryCard(StoryData.Story story, PuzzleUnlock unlock, int storyIndex) {
        // Get theme colors
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue, true);
        int surfaceColor = typedValue.data;

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int onSurfaceColor = typedValue.data;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundColor(surfaceColor);
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
        imageView.setImageResource(story.imageResId);

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

        TextView storyName = new TextView(this);
        storyName.setText(story.name);
        storyName.setTextSize(18);
        storyName.setTextColor(onSurfaceColor);
        storyName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView statusText = new TextView(this);
        if (unlock.isUnlocked()) {
            String stars = "★".repeat(unlock.getStarsEarned()) + "☆".repeat(3 - unlock.getStarsEarned());
            statusText.setText(stars + " | " + unlock.getStarsEarned() + "/3 Stars");
            statusText.setTextColor(0xFF4CAF50);
        } else {
            statusText.setText("🔒 Costs " + story.starsRequired + " stars to unlock");
            statusText.setTextColor(onSurfaceColor);
            statusText.setAlpha(0.6f);
        }
        statusText.setTextSize(14);
        statusText.setPadding(0, 5, 0, 0);

        infoLayout.addView(storyName);
        infoLayout.addView(statusText);

        // Action button (Play or Unlock)
        Button actionBtn = new Button(this);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actionBtn.setLayoutParams(btnParams);

        if (unlock.isUnlocked()) {
            actionBtn.setText("Play");
            actionBtn.setBackgroundColor(storyMode.color);
            actionBtn.setTextColor(Color.WHITE);
            actionBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, StoryDifficultyActivity.class);
                intent.putExtra("STORY_ID", storyId);
                intent.putExtra("STORY_INDEX", storyIndex);
                intent.putExtra("story_NAME", story.name);
                intent.putExtra("IMAGE_RES_ID", story.imageResId);
                startActivity(intent);
            });
        } else {
            actionBtn.setText("Unlock");
            actionBtn.setBackgroundColor(0xFF2196F3);
            actionBtn.setTextColor(Color.WHITE);
            actionBtn.setOnClickListener(v -> showUnlockDialog(story, unlock, storyIndex));
        }

        card.addView(imageView);
        card.addView(infoLayout);
        card.addView(actionBtn);

        storiesContainer.addView(card);
    }

    private void showUnlockDialog(StoryData.Story story, PuzzleUnlock unlock, int storyIndex) {
        // Check if user has enough stars
        if (currentTotalStars < story.starsRequired) {
            new AlertDialog.Builder(this)
                    .setTitle("Not Enough Stars")
                    .setMessage(String.format("You need %d stars to unlock %s.\n\nYou currently have %d stars.\nNeed %d more stars!",
                            story.starsRequired, story.name, currentTotalStars, story.starsRequired - currentTotalStars))
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Unlock Story")
                .setMessage(String.format("Unlock %s?\n\nCost: %d ⭐\nYour Stars: %d ⭐\nRemaining: %d ⭐",
                        story.name, story.starsRequired, currentTotalStars, currentTotalStars - story.starsRequired))
                .setPositiveButton("Unlock", (dialog, which) -> unlockStory(story, unlock, storyIndex))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void unlockStory(StoryData.Story story, PuzzleUnlock unlock, int storyIndex) {
        new Thread(() -> {
            // Deduct stars from user progress
            UserProgress progress = database.gameDao().getUserProgress();
            progress.setTotalStars(progress.getTotalStars() - story.starsRequired);
            database.gameDao().updateUserProgress(progress);

            // Unlock the story
            unlock.setUnlocked(true);
            database.gameDao().updatePuzzleUnlock(unlock);

            runOnUiThread(() -> {
                Toast.makeText(this, story.name + " unlocked! " + story.starsRequired + " stars spent.", Toast.LENGTH_LONG).show();
                loadTotalStars();
                loadStories();
            });
        }).start();
    }
}