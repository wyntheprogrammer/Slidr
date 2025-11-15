package com.example.slidr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button easyBtn = findViewById(R.id.btnEasy);
        Button mediumBtn = findViewById(R.id.btnMedium);
        Button hardBtn = findViewById(R.id.btnHard);

        easyBtn.setOnClickListener(v -> startGame(3));
        mediumBtn.setOnClickListener(v -> startGame(4));
        hardBtn.setOnClickListener(v -> startGame(5));
    }

    private void startGame(int gridSize) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("GRID_SIZE", gridSize);
        startActivity(intent);
    }
}