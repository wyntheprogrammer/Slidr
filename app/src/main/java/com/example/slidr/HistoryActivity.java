package com.example.slidr;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.GameHistory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private AppDatabase database;
    private LinearLayout historyContainer;
    private TextView emptyStateText;
    private TextView totalGamesText;
    private Button filterAllBtn, filterEasyBtn, filterMediumBtn, filterHardBtn;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        database = AppDatabase.getInstance(this);

        TextView titleText = findViewById(R.id.tvHistoryTitle);
        totalGamesText = findViewById(R.id.tvTotalGames);
        historyContainer = findViewById(R.id.historyContainer);
        emptyStateText = findViewById(R.id.tvEmptyState);

        filterAllBtn = findViewById(R.id.btnFilterAll);
        filterEasyBtn = findViewById(R.id.btnFilterEasy);
        filterMediumBtn = findViewById(R.id.btnFilterMedium);
        filterHardBtn = findViewById(R.id.btnFilterHard);

        Button clearHistoryBtn = findViewById(R.id.btnClearHistory);
        Button backBtn = findViewById(R.id.btnBack);

        // Filter buttons
        filterAllBtn.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterButtons();
            loadHistory();
        });

        filterEasyBtn.setOnClickListener(v -> {
            currentFilter = "easy";
            updateFilterButtons();
            loadHistory();
        });

        filterMediumBtn.setOnClickListener(v -> {
            currentFilter = "medium";
            updateFilterButtons();
            loadHistory();
        });

        filterHardBtn.setOnClickListener(v -> {
            currentFilter = "hard";
            updateFilterButtons();
            loadHistory();
        });

        clearHistoryBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to clear all game history? This cannot be undone.")
                    .setPositiveButton("Clear", (dialog, which) -> clearHistory())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        backBtn.setOnClickListener(v -> finish());

        updateFilterButtons();
        loadHistory();
    }

    private void updateFilterButtons() {
        // Reset all buttons
        filterAllBtn.setBackgroundColor(0xFFE0E0E0);
        filterEasyBtn.setBackgroundColor(0xFFE0E0E0);
        filterMediumBtn.setBackgroundColor(0xFFE0E0E0);
        filterHardBtn.setBackgroundColor(0xFFE0E0E0);

        // Highlight selected button
        switch (currentFilter) {
            case "all":
                filterAllBtn.setBackgroundColor(0xFF2196F3);
                break;
            case "easy":
                filterEasyBtn.setBackgroundColor(0xFF4CAF50);
                break;
            case "medium":
                filterMediumBtn.setBackgroundColor(0xFFFF9800);
                break;
            case "hard":
                filterHardBtn.setBackgroundColor(0xFFF44336);
                break;
        }
    }

    private void loadHistory() {
        new Thread(() -> {
            List<GameHistory> allGames = database.gameDao().getAllGames();

            // Filter games based on difficulty
            List<GameHistory> filteredGames = allGames;
            if (!currentFilter.equals("all")) {
                int gridSize = 0;
                switch (currentFilter) {
                    case "easy": gridSize = 3; break;
                    case "medium": gridSize = 4; break;
                    case "hard": gridSize = 5; break;
                }

                final int filterSize = gridSize;
                filteredGames = new java.util.ArrayList<>();
                for (GameHistory game : allGames) {
                    if (game.getGridSize() == filterSize) {
                        filteredGames.add(game);
                    }
                }
            }

            final List<GameHistory> games = filteredGames;

            runOnUiThread(() -> {
                historyContainer.removeAllViews();

                if (games.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                    totalGamesText.setText("Total Games: 0");
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    totalGamesText.setText(String.format("Total Games: %d", games.size()));

                    for (GameHistory game : games) {
                        addGameHistoryCard(game);
                    }
                }
            });
        }).start();
    }

    private void addGameHistoryCard(GameHistory game) {
        // Get theme colors
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue, true);
        int surfaceColor = typedValue.data;

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int onSurfaceColor = typedValue.data;

        // Main card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(surfaceColor);
        card.setPadding(25, 25, 25, 25);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 15);
        card.setLayoutParams(cardParams);

        // Header row with difficulty and status
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView difficultyBadge = new TextView(this);
        String difficulty = game.getGridSize() == 3 ? "EASY" :
                game.getGridSize() == 4 ? "MEDIUM" : "HARD";
        difficultyBadge.setText(difficulty);
        difficultyBadge.setTextSize(14);
        difficultyBadge.setTextColor(0xFFFFFFFF);
        difficultyBadge.setTypeface(null, Typeface.BOLD);
        difficultyBadge.setPadding(20, 10, 20, 10);

        int badgeColor = game.getGridSize() == 3 ? 0xFF4CAF50 :
                game.getGridSize() == 4 ? 0xFFFF9800 : 0xFFF44336;
        difficultyBadge.setBackgroundColor(badgeColor);

        TextView statusBadge = new TextView(this);
        statusBadge.setText(game.isCompleted() ? "✓ COMPLETED" : "✗ INCOMPLETE");
        statusBadge.setTextSize(14);
        statusBadge.setTextColor(0xFFFFFFFF);
        statusBadge.setTypeface(null, Typeface.BOLD);
        statusBadge.setPadding(20, 10, 20, 10);
        statusBadge.setBackgroundColor(game.isCompleted() ? 0xFF2E7D32 : 0xFFC62828);

        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        statusParams.setMargins(15, 0, 0, 0);
        statusBadge.setLayoutParams(statusParams);

        headerRow.addView(difficultyBadge);
        headerRow.addView(statusBadge);

        // Date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date gameDate = new Date(game.getTimestamp());

        TextView dateText = new TextView(this);
        dateText.setText(String.format("📅 %s  •  🕐 %s",
                dateFormat.format(gameDate),
                timeFormat.format(gameDate)));
        dateText.setTextSize(14);
        dateText.setTextColor(onSurfaceColor);
        dateText.setAlpha(0.7f);
        dateText.setPadding(0, 15, 0, 15);

        // Stats row
        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setGravity(Gravity.CENTER_VERTICAL);

        // Moves stat
        LinearLayout movesContainer = new LinearLayout(this);
        movesContainer.setOrientation(LinearLayout.VERTICAL);
        movesContainer.setGravity(Gravity.CENTER);
        movesContainer.setPadding(15, 15, 15, 15);

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
        movesContainer.setBackgroundColor(typedValue.data);

        TextView movesLabel = new TextView(this);
        movesLabel.setText("MOVES");
        movesLabel.setTextSize(12);
        movesLabel.setTextColor(onSurfaceColor);
        movesLabel.setAlpha(0.6f);
        movesLabel.setGravity(Gravity.CENTER);

        TextView movesValue = new TextView(this);
        movesValue.setText(String.valueOf(game.getMoves()));
        movesValue.setTextSize(24);
        movesValue.setTextColor(onSurfaceColor);
        movesValue.setTypeface(null, Typeface.BOLD);
        movesValue.setGravity(Gravity.CENTER);

        movesContainer.addView(movesValue);
        movesContainer.addView(movesLabel);

        // Time stat
        LinearLayout timeContainer = new LinearLayout(this);
        timeContainer.setOrientation(LinearLayout.VERTICAL);
        timeContainer.setGravity(Gravity.CENTER);
        timeContainer.setPadding(15, 15, 15, 15);
        timeContainer.setBackgroundColor(typedValue.data);

        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        timeParams.setMargins(15, 0, 0, 0);
        timeContainer.setLayoutParams(timeParams);

        TextView timeLabel = new TextView(this);
        timeLabel.setText("TIME");
        timeLabel.setTextSize(12);
        timeLabel.setTextColor(onSurfaceColor);
        timeLabel.setAlpha(0.6f);
        timeLabel.setGravity(Gravity.CENTER);

        TextView timeValue = new TextView(this);
        timeValue.setText(formatTime(game.getTimeInSeconds()));
        timeValue.setTextSize(24);
        timeValue.setTextColor(onSurfaceColor);
        timeValue.setTypeface(null, Typeface.BOLD);
        timeValue.setGravity(Gravity.CENTER);

        timeContainer.addView(timeValue);
        timeContainer.addView(timeLabel);

        // Grid size stat
        LinearLayout gridContainer = new LinearLayout(this);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        gridContainer.setGravity(Gravity.CENTER);
        gridContainer.setPadding(15, 15, 15, 15);
        gridContainer.setBackgroundColor(typedValue.data);

        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        gridParams.setMargins(15, 0, 0, 0);
        gridContainer.setLayoutParams(gridParams);

        TextView gridLabel = new TextView(this);
        gridLabel.setText("GRID");
        gridLabel.setTextSize(12);
        gridLabel.setTextColor(onSurfaceColor);
        gridLabel.setAlpha(0.6f);
        gridLabel.setGravity(Gravity.CENTER);

        TextView gridValue = new TextView(this);
        gridValue.setText(game.getGridSize() + "×" + game.getGridSize());
        gridValue.setTextSize(24);
        gridValue.setTextColor(onSurfaceColor);
        gridValue.setTypeface(null, Typeface.BOLD);
        gridValue.setGravity(Gravity.CENTER);

        gridContainer.addView(gridValue);
        gridContainer.addView(gridLabel);

        statsRow.addView(movesContainer);
        statsRow.addView(timeContainer);
        statsRow.addView(gridContainer);

        // Add all elements to card
        card.addView(headerRow);
        card.addView(dateText);
        card.addView(statsRow);

        historyContainer.addView(card);
    }

    private void clearHistory() {
        new Thread(() -> {
            database.gameDao().deleteAllGames();

            runOnUiThread(() -> {
                loadHistory();
                new AlertDialog.Builder(this)
                        .setTitle("History Cleared")
                        .setMessage("All game history has been deleted.")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }).start();
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}