package com.example.slidr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slidr.database.AppDatabase;
import com.example.slidr.database.PuzzleUnlock;
import com.example.slidr.database.UserProgress;
import com.example.slidr.database.MusicTrack;
import com.example.slidr.utils.MusicManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageGameActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private ImageButton[][] buttons;
    private int gridSize; // Now dynamic based on difficulty
    private int emptyRow, emptyCol;
    private int moves = 0;
    private TextView movesText, timerText, difficultyText;

    private long startTime = 0;
    private long elapsedTime = 0;
    private Handler timerHandler = new Handler();
    private boolean isTimerRunning = false;

    private AppDatabase database;
    private String storyId;
    private int arcIndex;
    private String arcName;
    private int imageResId;
    private String difficulty; // "easy", "medium", "hard"
    private int starsToEarn; // 1, 2, or 3

    private Bitmap fullImage;
    private Bitmap[] imageTiles;
    private int[] currentTileOrder; // Track current tile positions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_game);

        database = AppDatabase.getInstance(this);

        storyId = getIntent().getStringExtra("STORY_ID");
        arcIndex = getIntent().getIntExtra("ARC_INDEX", 0);
        arcName = getIntent().getStringExtra("ARC_NAME");
        imageResId = getIntent().getIntExtra("IMAGE_RES_ID", 0);
        difficulty = getIntent().getStringExtra("DIFFICULTY");
        starsToEarn = getIntent().getIntExtra("STARS_TO_EARN", 1);
        gridSize = getIntent().getIntExtra("GRID_SIZE", 4); // Get grid size from intent

        gridLayout = findViewById(R.id.gridLayout);
        movesText = findViewById(R.id.tvMoves);
        timerText = findViewById(R.id.tvTimer);
        difficultyText = findViewById(R.id.tvDifficulty);
        TextView titleText = findViewById(R.id.tvArcTitle);
        Button shuffleBtn = findViewById(R.id.btnShuffle);
        Button backBtn = findViewById(R.id.btnBack);

        titleText.setText(arcName);

        String diffText = difficulty.substring(0, 1).toUpperCase() + difficulty.substring(1);
        String stars = "⭐".repeat(starsToEarn);
        difficultyText.setText(diffText + " " + stars);

        gridLayout.setColumnCount(gridSize);
        gridLayout.setRowCount(gridSize);

        buttons = new ImageButton[gridSize][gridSize];

        loadImage();
        initializeGame();
//        startArcMusic(); // NEW: Play music for this arc

        shuffleBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("New Game")
                    .setMessage("Start a new game? Current progress will be lost.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        stopTimer();
                        moves = 0;
                        elapsedTime = 0;
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
    }

    private void loadImage() {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), imageResId);

        int size = Math.min(originalBitmap.getWidth(), originalBitmap.getHeight());
        int xOffset = (originalBitmap.getWidth() - size) / 2;
        int yOffset = (originalBitmap.getHeight() - size) / 2;

        fullImage = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, size, size);
        fullImage = Bitmap.createScaledBitmap(fullImage, 900, 900, true);

        splitImageIntoTiles();
    }

    private void splitImageIntoTiles() {
        imageTiles = new Bitmap[gridSize * gridSize];
        int tileSize = 900 / gridSize;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int index = i * gridSize + j;

                if (i == gridSize - 1 && j == gridSize - 1) {
                    // Empty tile
                    imageTiles[index] = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(imageTiles[index]);
                    canvas.drawColor(Color.LTGRAY);
                } else {
                    imageTiles[index] = Bitmap.createBitmap(
                            fullImage,
                            j * tileSize,
                            i * tileSize,
                            tileSize,
                            tileSize
                    );

                    // Add number overlay
                    imageTiles[index] = addNumberOverlay(imageTiles[index], index + 1);
                }
            }
        }
    }

    private Bitmap addNumberOverlay(Bitmap bitmap, int number) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setShadowLayer(5, 2, 2, Color.BLACK);

        canvas.drawText(
                String.valueOf(number),
                bitmap.getWidth() - 30,
                40,
                paint
        );

        return mutableBitmap;
    }

    private void initializeGame() {
        gridLayout.removeAllViews();
        int tileSize = 900 / gridSize;

        currentTileOrder = new int[gridSize * gridSize];

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                ImageButton btn = new ImageButton(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = tileSize;
                params.height = tileSize;
                params.setMargins(2, 2, 2, 2);
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);

                btn.setLayoutParams(params);
                btn.setScaleType(ImageButton.ScaleType.FIT_XY);
                btn.setPadding(0, 0, 0, 0);

                int index = i * gridSize + j;
                btn.setImageBitmap(imageTiles[index]);
                currentTileOrder[index] = index;

                if (i == gridSize - 1 && j == gridSize - 1) {
                    emptyRow = i;
                    emptyCol = j;
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
        updateMovesText();
        updateTimerText();
        shufflePuzzle();
        startTimer();
    }

    private void onTileClick(int row, int col) {
        if (isAdjacent(row, col, emptyRow, emptyCol)) {
            swapTiles(row, col, emptyRow, emptyCol);

            // Update tracking array
            int clickedIndex = row * gridSize + col;
            int emptyIndex = emptyRow * gridSize + emptyCol;
            int temp = currentTileOrder[clickedIndex];
            currentTileOrder[clickedIndex] = currentTileOrder[emptyIndex];
            currentTileOrder[emptyIndex] = temp;

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

    private boolean isAdjacent(int r1, int c1, int r2, int c2) {
        return (Math.abs(r1 - r2) == 1 && c1 == c2) || (Math.abs(c1 - c2) == 1 && r1 == r2);
    }

    private void swapTiles(int r1, int c1, int r2, int c2) {
        BitmapDrawable drawable1 = (BitmapDrawable) buttons[r1][c1].getDrawable();
        BitmapDrawable drawable2 = (BitmapDrawable) buttons[r2][c2].getDrawable();

        buttons[r1][c1].setImageBitmap(drawable2.getBitmap());
        buttons[r2][c2].setImageBitmap(drawable1.getBitmap());
    }

    private void shufflePuzzle() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < gridSize * gridSize - 1; i++) {
            numbers.add(i);
        }

        do {
            Collections.shuffle(numbers);
        } while (!isSolvable(numbers));

        // Reset the tracking array and apply shuffle
        int index = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int pos = i * gridSize + j;
                if (i == gridSize - 1 && j == gridSize - 1) {
                    buttons[i][j].setImageBitmap(imageTiles[gridSize * gridSize - 1]);
                    currentTileOrder[pos] = gridSize * gridSize - 1;
                } else {
                    int tileIndex = numbers.get(index);
                    buttons[i][j].setImageBitmap(imageTiles[tileIndex]);
                    currentTileOrder[pos] = tileIndex;
                    index++;
                }
            }
        }

        // Reset empty position
        emptyRow = gridSize - 1;
        emptyCol = gridSize - 1;
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
            // Odd grid (3x3, 5x5): solvable if inversions is even
            return inversions % 2 == 0;
        } else {
            // Even grid (4x4): more complex check
            return (inversions + gridSize) % 2 == 1;
        }
    }

    private boolean isSolved() {
        for (int i = 0; i < gridSize * gridSize; i++) {
            if (currentTileOrder[i] != i) {
                return false;
            }
        }
        return true;
    }

    private void onGameCompleted() {
        long timeInSeconds = elapsedTime / 1000;

        // Update database - always reward stars based on difficulty
        new Thread(() -> {
            PuzzleUnlock unlock = database.gameDao().getPuzzleUnlock(storyId, arcIndex);

            boolean earnedNewStars = false;
            if (starsToEarn > unlock.getStarsEarned()) {
                int starDifference = starsToEarn - unlock.getStarsEarned();

                unlock.setStarsEarned(starsToEarn);
                earnedNewStars = true;

                // Update total stars
                UserProgress progress = database.gameDao().getUserProgress();
                progress.setTotalStars(progress.getTotalStars() + starDifference);
                database.gameDao().updateUserProgress(progress);

                // Unlock music for this arc
                unlockMusic(storyId, arcIndex);

                // Check and unlock next arc
                unlockNextArc(progress.getTotalStars());
            }

            // Always update best scores
            if (moves < unlock.getBestMoves()) {
                unlock.setBestMoves(moves);
            }
            if (timeInSeconds < unlock.getBestTime()) {
                unlock.setBestTime(timeInSeconds);
            }

            database.gameDao().updatePuzzleUnlock(unlock);

            final boolean finalEarnedStars = earnedNewStars;
            runOnUiThread(() -> showCompletionDialog(starsToEarn, timeInSeconds, finalEarnedStars));
        }).start();
    }

    private void unlockMusic(String storyId, int arcIndex) {
        MusicTrack track = database.gameDao().getMusicForArc(storyId, arcIndex);
        if (track != null && !track.isUnlocked()) {
            track.setUnlocked(true);
            database.gameDao().updateMusicTrack(track);
        }
    }

    private void unlockNextArc(int totalStars) {
        // Get all story modes and check what can be unlocked
        String[] stories = {"onepiece", "dragonball", "bleach"};

        for (String story : stories) {
            List<PuzzleUnlock> unlocks = database.gameDao().getStoryModeProgress(story);
            com.example.slidr.models.StoryData.StoryMode mode =
                    com.example.slidr.models.StoryData.getStoryMode(story);

            for (int i = 0; i < unlocks.size(); i++) {
                PuzzleUnlock unlock = unlocks.get(i);
                if (!unlock.isUnlocked() && totalStars >= mode.arcs[i].starsRequired) {
                    unlock.setUnlocked(true);
                    database.gameDao().updatePuzzleUnlock(unlock);
                }
            }
        }
    }

    private void showCompletionDialog(int stars, long timeInSeconds, boolean earnedNewStars) {
        String starDisplay = "⭐".repeat(stars);
        String message = String.format(
                "%s Earned!\n\nMoves: %d\nTime: %s",
                starDisplay, moves, formatTime(timeInSeconds)
        );

        if (earnedNewStars) {
            message += "\n\n🎉 NEW STARS EARNED! 🎉\n🎵 Music Unlocked!";
        } else {
            message += "\n\n(Already earned " + stars + " star" + (stars > 1 ? "s" : "") + " on this arc)";
        }

        new AlertDialog.Builder(this)
                .setTitle("Puzzle Complete!")
                .setMessage(message)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    moves = 0;
                    elapsedTime = 0;
                    updateMovesText();
                    updateTimerText();
                    shufflePuzzle();
                    startTimer();
                })
                .setNegativeButton("Back", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}