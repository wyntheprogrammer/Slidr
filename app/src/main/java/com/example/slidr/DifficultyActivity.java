package com.example.slidr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class DifficultyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        Button easyBtn = findViewById(R.id.btnEasy);
        Button mediumBtn = findViewById(R.id.btnMedium);
        Button hardBtn = findViewById(R.id.btnHard);
        Button backBtn = findViewById(R.id.btnBack);

        easyBtn.setOnClickListener(v -> startGame(3));
        mediumBtn.setOnClickListener(v -> startGame(4));
        hardBtn.setOnClickListener(v -> startGame(5));

        backBtn.setOnClickListener(v -> finish());
    }

    private void startGame(int gridSize) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("GRID_SIZE", gridSize);
        intent.putExtra("MODE", "classic");
        startActivity(intent);
    }
}