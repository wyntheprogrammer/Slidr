package com.example.slidr;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private Button[][] buttons;
    private int gridSize;
    private int emptyRow, emptyCol;
    private int moves = 0;
    private TextView movesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gridSize = getIntent().getIntExtra("GRID_SIZE", 3);

        gridLayout = findViewById(R.id.gridLayout);
        movesText = findViewById(R.id.tvMoves);
        Button shuffleBtn = findViewById(R.id.btnShuffle);
        Button backBtn = findViewById(R.id.btnBack);

        gridLayout.setColumnCount(gridSize);
        gridLayout.setRowCount(gridSize);

        buttons = new Button[gridSize][gridSize];

        initializeGame();

        shuffleBtn.setOnClickListener(v -> {
            moves = 0;
            updateMovesText();
            shufflePuzzle();
        });

        backBtn.setOnClickListener(v -> finish());
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
        updateMovesText();
        shufflePuzzle();
    }

    private void onTileClick(int row, int col) {
        if (isAdjacent(row, col, emptyRow, emptyCol)) {
            swapTiles(row, col, emptyRow, emptyCol);
            emptyRow = row;
            emptyCol = col;
            moves++;
            updateMovesText();

            if (isSolved()) {
                Toast.makeText(this, "Congratulations! You solved it in " + moves + " moves!", Toast.LENGTH_LONG).show();
            }
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

    private void updateMovesText() {
        movesText.setText("Moves: " + moves);
    }

}