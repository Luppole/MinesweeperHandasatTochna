package com.example.minesweeperhansasattochna;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private Cell[][] board;
    private int ROWS, COLS, NUM_MINES;
    private GridLayout gridLayout;
    private TextView timer;
    private Button[][] buttons;
    private Handler timerHandler = new Handler();
    private boolean gameRunning = false;
    private int timeElapsed = 0; // Timer counter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Get difficulty from Intent
        String difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = "easy"; // Default difficulty
        }
        setDifficulty(difficulty);

        gridLayout = findViewById(R.id.gridLayout);
        timer = findViewById(R.id.timer);

        initializeBoard();
        setupGrid();

        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> {
            resetGame();
            Toast.makeText(GameActivity.this, "Game Reset!", Toast.LENGTH_SHORT).show();
        });

        startTimer();
    }

    private void setDifficulty(String difficulty) {
        switch (difficulty) {
            case "medium":
                ROWS = 9;
                COLS = 9;
                NUM_MINES = 15;
                break;
            case "hard":
                ROWS = 12;
                COLS = 12;
                NUM_MINES = 25;
                break;
            case "easy":
            default:
                ROWS = 6;
                COLS = 6;
                NUM_MINES = 8;
                break;
        }
    }

    private void initializeBoard() {
        board = new Cell[ROWS][COLS];
        buttons = new Button[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = new Cell();
            }
        }
        placeMines();
        calculateAdjacentMines();
        gameRunning = true;
    }

    private void placeMines() {
        Random random = new Random();
        int placedMines = 0;
        while (placedMines < NUM_MINES) {
            int row = random.nextInt(ROWS);
            int col = random.nextInt(COLS);

            if (!board[row][col].isMine) {
                board[row][col].isMine = true;
                placedMines++;
            }
        }
    }

    private void calculateAdjacentMines() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (!board[i][j].isMine) {
                    board[i][j].adjacentMines = countAdjacentMines(i, j);
                }
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        int[] directions = {-1, 0, 1};

        for (int dr : directions) {
            for (int dc : directions) {
                int newRow = row + dr;
                int newCol = col + dc;

                if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS) {
                    if (board[newRow][newCol].isMine) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void setupGrid() {
        gridLayout.removeAllViews();
        gridLayout.setRowCount(ROWS);
        gridLayout.setColumnCount(COLS);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        int gridSize = Math.min(screenWidth, screenHeight) - 400; // Adjusted size
        int tileSize = gridSize / Math.max(ROWS, COLS);

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Button cellButton = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);
                params.width = tileSize;
                params.height = tileSize;
                cellButton.setLayoutParams(params);

                int finalI = i;
                int finalJ = j;
                cellButton.setOnClickListener(v -> revealCell(finalI, finalJ));
                cellButton.setOnLongClickListener(v -> {
                    flagCell(finalI, finalJ);
                    return true;
                });

                gridLayout.addView(cellButton);
                buttons[i][j] = cellButton;
            }
        }
    }

    private boolean checkWinCondition() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (!board[i][j].isMine && !board[i][j].isRevealed) {
                    return false;
                }
            }
        }
        return true;
    }

    private void revealCell(int row, int col) {
        if (!gameRunning || board[row][col].isRevealed || board[row][col].isFlagged) return;

        board[row][col].isRevealed = true;

        if (board[row][col].isMine) {
            gameRunning = false;
            Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show();
            revealAllMines();
            stopTimer();
            recordGameResult(false);
            return;
        }

        buttons[row][col].setText(String.valueOf(board[row][col].adjacentMines));
        buttons[row][col].setEnabled(false);

        if (board[row][col].adjacentMines == 0) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int newRow = row + dr;
                    int newCol = col + dc;
                    if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS) {
                        revealCell(newRow, newCol);
                    }
                }
            }
        }

        if (checkWinCondition()) {
            gameRunning = false;
            Toast.makeText(this, "You Win!", Toast.LENGTH_SHORT).show();
            stopTimer();
            recordGameResult(true);
        }
    }

    private void flagCell(int row, int col) {
        if (!gameRunning || board[row][col].isRevealed) return;

        board[row][col].isFlagged = !board[row][col].isFlagged;
        buttons[row][col].setText(board[row][col].isFlagged ? "F" : "");
    }

    private void revealAllMines() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (board[i][j].isMine) {
                    buttons[i][j].setText("M");
                }
            }
        }
    }

    private void startTimer() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (gameRunning) {
                    timeElapsed++;
                    timer.setText("Time: " + timeElapsed);
                    timerHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void stopTimer() {
        timerHandler.removeCallbacksAndMessages(null);
    }

    private void resetGame() {
        timeElapsed = 0;
        initializeBoard();
        setupGrid();
        startTimer();
    }

    private void recordGameResult(boolean won) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "guest";

        if (userEmail != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app").getReference("gameHistory");

            HashMap<String, Object> gameData = new HashMap<>();
            gameData.put("won", won);
            gameData.put("timeTaken", timeElapsed);
            gameData.put("difficulty", getIntent().getStringExtra("difficulty"));
            gameData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date()));

            dbRef.child(userEmail.replace(".", ",")).push().setValue(gameData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(GameActivity.this, "Game result saved!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(GameActivity.this, "Failed to save game result: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
