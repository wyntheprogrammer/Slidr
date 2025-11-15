package com.example.slidr;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.GameHistory;
import com.example.slidr.database.Statistics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private AppDatabase database;
    private LinearLayout statsContainer;
    private TextView overallStatsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        database = AppDatabase.getInstance(this);
        statsContainer = findViewById(R.id.statsContainer);
        overallStatsText = findViewById(R.id.tvOverallStats);
        Button backBtn = findViewById(R.id.btnBack);
        Button clearBtn = findViewById(R.id.btnClearData);
        Button historyBtn = findViewById(R.id.btnHistory);

        loadStatistics();

        backBtn.setOnClickListener(v -> finish());

        clearBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Data")
                    .setMessage("Are you sure you want to clear all statistics and game history? This cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> clearAllData())
                    .setNegativeButton("No", null)
                    .show();
        });

        historyBtn.setOnClickListener(v -> showGameHistory());
    }

    private void loadStatistics() {
        new Thread(() -> {
            List<Statistics> allStats = database.gameDao().getAllStatistics();

            runOnUiThread(() -> {
                statsContainer.removeAllViews();

                if (allStats.isEmpty()) {
                    TextView noData = new TextView(this);
                    noData.setText("No statistics available yet.\nStart playing to see your stats!");
                    noData.setTextSize(18);
                    noData.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                    noData.setPadding(20, 40, 20, 40);
                    statsContainer.addView(noData);
                } else {
                    int totalGames = 0;
                    int totalCompleted = 0;

                    for (Statistics stats : allStats) {
                        totalGames += stats.getGamesPlayed();
                        totalCompleted += stats.getGamesCompleted();
                        addStatisticsCard(stats);
                    }

                    overallStatsText.setText(String.format(
                            "Overall: %d games played | %d completed",
                            totalGames, totalCompleted
                    ));
                }
            });
        }).start();
    }

    private void addStatisticsCard(Statistics stats) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFF5F5F5);
        card.setPadding(30, 30, 30, 30);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 20);
        card.setLayoutParams(params);

        String difficulty = stats.getGridSize() == 3 ? "Easy (3x3)" :
                stats.getGridSize() == 4 ? "Medium (4x4)" : "Hard (5x5)";

        TextView title = new TextView(this);
        title.setText(difficulty);
        title.setTextSize(22);
        title.setTextColor(0xFF333333);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);

        TextView details = new TextView(this);
        int avgMoves = stats.getGamesCompleted() > 0 ?
                stats.getTotalMoves() / stats.getGamesCompleted() : 0;
        long avgTime = stats.getGamesCompleted() > 0 ?
                stats.getTotalTime() / stats.getGamesCompleted() : 0;

        String detailsText = String.format(
                "Games Played: %d\nCompleted: %d\n\nBest Moves: %d\nBest Time: %s\n\nAverage Moves: %d\nAverage Time: %s",
                stats.getGamesPlayed(),
                stats.getGamesCompleted(),
                stats.getBestMoves() != Integer.MAX_VALUE ? stats.getBestMoves() : 0,
                stats.getBestTime() != Long.MAX_VALUE ? formatTime(stats.getBestTime()) : "N/A",
                avgMoves,
                formatTime(avgTime)
        );

        details.setText(detailsText);
        details.setTextSize(16);
        details.setTextColor(0xFF666666);
        details.setPadding(0, 10, 0, 0);

        card.addView(title);
        card.addView(details);
        statsContainer.addView(card);
    }

    private void showGameHistory() {
        new Thread(() -> {
            List<GameHistory> games = database.gameDao().getAllGames();

            runOnUiThread(() -> {
                if (games.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Game History")
                            .setMessage("No games in history yet!")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    StringBuilder history = new StringBuilder();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

                    int count = 0;
                    for (GameHistory game : games) {
                        if (count >= 20) break; // Show only last 20 games

                        String difficulty = game.getGridSize() == 3 ? "Easy" :
                                game.getGridSize() == 4 ? "Medium" : "Hard";

                        history.append(String.format("%s - %s\nMoves: %d | Time: %s\n%s\n\n",
                                difficulty,
                                dateFormat.format(new Date(game.getTimestamp())),
                                game.getMoves(),
                                formatTime(game.getTimeInSeconds()),
                                game.isCompleted() ? "✓ Completed" : "✗ Incomplete"
                        ));
                        count++;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Recent Games")
                            .setMessage(history.toString())
                            .setPositiveButton("OK", null)
                            .show();
                }
            });
        }).start();
    }

    private void clearAllData() {
        new Thread(() -> {
            database.gameDao().deleteAllGames();
            database.gameDao().deleteAllStatistics();

            runOnUiThread(() -> {
                loadStatistics();
                overallStatsText.setText("All data cleared");
            });
        }).start();
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}