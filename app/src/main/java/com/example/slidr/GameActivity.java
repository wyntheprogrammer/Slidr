package com.example.slidr;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.GameHistory;
import com.example.slidr.database.Statistics;
import com.example.slidr.utils.MusicManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class GameActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private Button[][] buttons;
    private int gridSize;
    private int emptyRow, emptyCol;
    private int moves = 0;
    private TextView movesText, timerText, bestScoreText;

    // Timer
    private long startTime = 0;
    private long elapsedTime = 0;
    private Handler timerHandler = new Handler();
    private boolean isTimerRunning = false;

    // Undo feature
    private Stack<Move> moveHistory = new Stack<>();

    // Database
    private AppDatabase database;

    // Best scores
    private int bestMoves = Integer.MAX_VALUE;
    private long bestTime = Long.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gridSize = getIntent().getIntExtra("GRID_SIZE", 3);

        database = AppDatabase.getInstance(this);

        gridLayout = findViewById(R.id.gridLayout);
        movesText = findViewById(R.id.tvMoves);
        timerText = findViewById(R.id.tvTimer);
        bestScoreText = findViewById(R.id.tvBestScore);
        Button shuffleBtn = findViewById(R.id.btnShuffle);
        Button backBtn = findViewById(R.id.btnBack);
        Button undoBtn = findViewById(R.id.btnUndo);
        Button statsBtn = findViewById(R.id.btnStats);

        gridLayout.setColumnCount(gridSize);
        gridLayout.setRowCount(gridSize);

        buttons = new Button[gridSize][gridSize];

        loadBestScores();
        initializeGame();
        startBackgroundMusic(); // NEW: Start music

        shuffleBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("New Game")
                    .setMessage("Start a new game? Current progress will be lost.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        stopTimer();
                        moves = 0;
                        elapsedTime = 0;
                        moveHistory.clear();
                        updateMovesText();
                        updateTimerText();
                        shufflePuzzle();
                        startTimer();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        backBtn.setOnClickListener(v -> {
            stopTimer();
            finish();
        });

        undoBtn.setOnClickListener(v -> undoLastMove());

        statsBtn.setOnClickListener(v -> showStatistics());
    }

    private void initializeGame() {
        gridLayout.removeAllViews();

        int tileSize = 900 / gridSize;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Button btn = new Button(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = tileSize;
                params.height = tileSize;
                params.setMargins(4, 4, 4, 4);
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);

                btn.setLayoutParams(params);
                btn.setTextSize(24);
                btn.setGravity(Gravity.CENTER);

                int number = i * gridSize + j + 1;

                if (i == gridSize - 1 && j == gridSize - 1) {
                    btn.setText("");
                    btn.setBackgroundColor(Color.LTGRAY);
                    emptyRow = i;
                    emptyCol = j;
                } else {
                    btn.setText(String.valueOf(number));
                    btn.setBackgroundColor(Color.parseColor("#4CAF50"));
                    btn.setTextColor(Color.WHITE);
                }

                final int row = i;
                final int col = j;
                btn.setOnClickListener(v -> onTileClick(row, col));

                buttons[i][j] = btn;
                gridLayout.addView(btn);
            }
        }

        moves = 0;
        elapsedTime = 0;
        moveHistory.clear();
        updateMovesText();
        updateTimerText();
        shufflePuzzle();
        startTimer();
    }

    private void onTileClick(int row, int col) {
        if (isAdjacent(row, col, emptyRow, emptyCol)) {
            moveHistory.push(new Move(row, col, emptyRow, emptyCol));
            swapTiles(row, col, emptyRow, emptyCol);
            emptyRow = row;
            emptyCol = col;
            moves++;
            updateMovesText();

            if (isSolved()) {
                stopTimer();
                onGameCompleted();
            }
        }
    }

    private void undoLastMove() {
        if (!moveHistory.isEmpty() && moves > 0) {
            Move lastMove = moveHistory.pop();
            swapTiles(lastMove.emptyRow, lastMove.emptyCol, lastMove.tileRow, lastMove.tileCol);
            emptyRow = lastMove.emptyRow;
            emptyCol = lastMove.emptyCol;
            moves--;
            updateMovesText();
        } else {
            Toast.makeText(this, "No moves to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isAdjacent(int r1, int c1, int r2, int c2) {
        return (Math.abs(r1 - r2) == 1 && c1 == c2) || (Math.abs(c1 - c2) == 1 && r1 == r2);
    }

    private void swapTiles(int r1, int c1, int r2, int c2) {
        String temp = buttons[r1][c1].getText().toString();
        int tempColor = ((android.graphics.drawable.ColorDrawable) buttons[r1][c1].getBackground()).getColor();

        buttons[r1][c1].setText(buttons[r2][c2].getText());
        buttons[r1][c1].setBackgroundColor(((android.graphics.drawable.ColorDrawable) buttons[r2][c2].getBackground()).getColor());

        buttons[r2][c2].setText(temp);
        buttons[r2][c2].setBackgroundColor(tempColor);
    }

    private void shufflePuzzle() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i < gridSize * gridSize; i++) {
            numbers.add(i);
        }

        do {
            Collections.shuffle(numbers);
        } while (!isSolvable(numbers));

        int index = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (i == gridSize - 1 && j == gridSize - 1) {
                    buttons[i][j].setText("");
                    buttons[i][j].setBackgroundColor(Color.LTGRAY);
                } else {
                    buttons[i][j].setText(String.valueOf(numbers.get(index++)));
                    buttons[i][j].setBackgroundColor(Color.parseColor("#4CAF50"));
                    buttons[i][j].setTextColor(Color.WHITE);
                }
            }
        }
    }

    private boolean isSolvable(List<Integer> tiles) {
        int inversions = 0;
        for (int i = 0; i < tiles.size(); i++) {
            for (int j = i + 1; j < tiles.size(); j++) {
                if (tiles.get(i) > tiles.get(j)) {
                    inversions++;
                }
            }
        }

        if (gridSize % 2 == 1) {
            return inversions % 2 == 0;
        } else {
            return (inversions + gridSize) % 2 == 1;
        }
    }

    private boolean isSolved() {
        int expectedNumber = 1;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (i == gridSize - 1 && j == gridSize - 1) {
                    return buttons[i][j].getText().toString().isEmpty();
                }

                String text = buttons[i][j].getText().toString();
                if (text.isEmpty() || Integer.parseInt(text) != expectedNumber) {
                    return false;
                }
                expectedNumber++;
            }
        }
        return true;
    }

    private void onGameCompleted() {
        long timeInSeconds = elapsedTime / 1000;

        // Save to database
        saveGameToDatabase(true, timeInSeconds);
        updateStatistics(true, timeInSeconds);

        // Check if new record
        boolean newBestMoves = moves < bestMoves;
        boolean newBestTime = timeInSeconds < bestTime;

        String message = String.format("Congratulations!\n\nMoves: %d\nTime: %s",
                moves, formatTime(timeInSeconds));

        if (newBestMoves || newBestTime) {
            message += "\n\n🏆 NEW RECORD! 🏆";
            if (newBestMoves) message += "\nBest Moves!";
            if (newBestTime) message += "\nBest Time!";
            loadBestScores(); // Reload best scores
        }

        new AlertDialog.Builder(this)
                .setTitle("Puzzle Solved!")
                .setMessage(message)
                .setPositiveButton("New Game", (dialog, which) -> {
                    moves = 0;
                    elapsedTime = 0;
                    moveHistory.clear();
                    updateMovesText();
                    updateTimerText();
                    shufflePuzzle();
                    startTimer();
                })
                .setNegativeButton("Main Menu", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void saveGameToDatabase(boolean completed, long timeInSeconds) {
        GameHistory game = new GameHistory(
                gridSize,
                moves,
                timeInSeconds,
                System.currentTimeMillis(),
                completed
        );

        new Thread(() -> database.gameDao().insertGame(game)).start();
    }

    private void updateStatistics(boolean completed, long timeInSeconds) {
        new Thread(() -> {
            Statistics stats = database.gameDao().getStatistics(gridSize);

            if (stats == null) {
                stats = new Statistics(gridSize);
            }

            stats.setGamesPlayed(stats.getGamesPlayed() + 1);

            if (completed) {
                stats.setGamesCompleted(stats.getGamesCompleted() + 1);
                stats.setTotalMoves(stats.getTotalMoves() + moves);
                stats.setTotalTime(stats.getTotalTime() + timeInSeconds);

                if (moves < stats.getBestMoves()) {
                    stats.setBestMoves(moves);
                }

                if (timeInSeconds < stats.getBestTime()) {
                    stats.setBestTime(timeInSeconds);
                }
            }

            if (stats.getGamesPlayed() == 1) {
                database.gameDao().insertStatistics(stats);
            } else {
                database.gameDao().updateStatistics(stats);
            }
        }).start();
    }

    private void loadBestScores() {
        new Thread(() -> {
            Statistics stats = database.gameDao().getStatistics(gridSize);
            if (stats != null) {
                bestMoves = stats.getBestMoves();
                bestTime = stats.getBestTime();

                runOnUiThread(() -> {
                    if (bestMoves != Integer.MAX_VALUE && bestTime != Long.MAX_VALUE) {
                        bestScoreText.setText(String.format("Best: %d moves | %s",
                                bestMoves, formatTime(bestTime)));
                    } else {
                        bestScoreText.setText("No records yet");
                    }
                });
            }
        }).start();
    }

    private void showStatistics() {
        new Thread(() -> {
            Statistics stats = database.gameDao().getStatistics(gridSize);

            runOnUiThread(() -> {
                String message;
                if (stats == null || stats.getGamesPlayed() == 0) {
                    message = "No games played yet!";
                } else {
                    int avgMoves = stats.getGamesCompleted() > 0 ?
                            stats.getTotalMoves() / stats.getGamesCompleted() : 0;
                    long avgTime = stats.getGamesCompleted() > 0 ?
                            stats.getTotalTime() / stats.getGamesCompleted() : 0;

                    message = String.format(
                            "Games Played: %d\nCompleted: %d\n\nBest Moves: %d\nBest Time: %s\n\nAverage Moves: %d\nAverage Time: %s",
                            stats.getGamesPlayed(),
                            stats.getGamesCompleted(),
                            stats.getBestMoves() != Integer.MAX_VALUE ? stats.getBestMoves() : 0,
                            stats.getBestTime() != Long.MAX_VALUE ? formatTime(stats.getBestTime()) : "N/A",
                            avgMoves,
                            formatTime(avgTime)
                    );
                }

                new AlertDialog.Builder(this)
                        .setTitle("Statistics")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show();
            });
        }).start();
    }

    // Timer methods
    private void startTimer() {
        startTime = System.currentTimeMillis();
        isTimerRunning = true;
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTimerRunning) {
                elapsedTime = System.currentTimeMillis() - startTime;
                updateTimerText();
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    private void updateTimerText() {
        long seconds = elapsedTime / 1000;
        timerText.setText("Time: " + formatTime(seconds));
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private void updateMovesText() {
        movesText.setText("Moves: " + moves);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
        MusicManager.pauseMusic(); // NEW: Pause music
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.resumeMusic(); // NEW: Resume music

        // If music was never started, start it
        if (!MusicManager.isPlaying()) {
            startBackgroundMusic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        MusicManager.stopMusic(); // NEW: Stop music when leaving
    }

    // NEW: Start background music based on settings
    private void startBackgroundMusic() {
        new Thread(() -> {
            try {
                com.example.slidr.database.GameSettings settings = database.gameDao().getSettings();
                if (settings != null && settings.isMusicEnabled() && settings.getSelectedMusicId() != -1) {
                    com.example.slidr.database.MusicTrack track = database.gameDao().getMusicTrack(settings.getSelectedMusicId());
                    if (track != null && track.isUnlocked()) {
                        runOnUiThread(() -> {
                            MusicManager.playMusic(this, track.getMusicResId());
                        });
                    }
                }
            } catch (Exception e) {
                // Music is optional, don't crash if it fails
                e.printStackTrace();
            }
        }).start();
    }

    // Helper class for undo feature
    private static class Move {
        int tileRow, tileCol;
        int emptyRow, emptyCol;

        Move(int tileRow, int tileCol, int emptyRow, int emptyCol) {
            this.tileRow = tileRow;
            this.tileCol = tileCol;
            this.emptyRow = emptyRow;
            this.emptyCol = emptyCol;
        }
    }
}