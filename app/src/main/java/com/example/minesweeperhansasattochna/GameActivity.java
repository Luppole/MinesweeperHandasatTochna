package com.example.minesweeperhansasattochna;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private Cell[][] board;
    private int ROWS, COLS, NUM_MINES;
    private GridLayout gridLayout;
    private TextView timer, personalBestView, bombCounterView, pointsView;
    private LinearLayout itemsBoard; // Items Board for displaying available items
    private Button[][] buttons;
    private ImageButton homeButton;
    private Handler timerHandler = new Handler();
    private boolean gameRunning = false;
    private int timeElapsed = 0;
    private int bombsLeft;

    private DatabaseReference userRef, leaderboardRef;
    private String difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize views
        gridLayout = findViewById(R.id.gridLayout);
        timer = findViewById(R.id.timer);
        personalBestView = findViewById(R.id.personalBest);
        bombCounterView = findViewById(R.id.bombCounter);
        pointsView = findViewById(R.id.points);
        // itemsBoard = findViewById(R.id.itemsBoard); // Initialize items board
        homeButton = findViewById(R.id.homeButton);
        Button leaderboardButton = findViewById(R.id.leaderboardButton);
        Button gameHistoryButton = findViewById(R.id.gameHistoryButton);
        Button resetButton = findViewById(R.id.resetButton);

        // Get difficulty level
        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = "easy";
        }

        // Set up home button
        homeButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(homeIntent);
        });

        // Firebase setup
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "guest";
        userRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users")
                .child(userEmail.replace(".", ","));
        leaderboardRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("leaderboard");

        // Fetch points and update UI
        fetchUserPoints();

        // Button listeners
        leaderboardButton.setOnClickListener(v -> {
            animateButtonClick(v); // Animate button click
            Intent leaderboardIntent = new Intent(GameActivity.this, LeaderboardActivity.class);
            startActivity(leaderboardIntent);
        });

        gameHistoryButton.setOnClickListener(v -> {
            animateButtonClick(v); // Animate button click
            Intent historyIntent = new Intent(GameActivity.this, GameHistoryActivity.class);
            startActivity(historyIntent);
        });

        resetButton.setOnClickListener(v -> {
            animateButtonClick(v); // Animate button click
            resetGame();
        });

        // Other initializations
        setDifficulty(difficulty);
        fetchPersonalBest();
        initializeBoard();
        setupGrid();
        startTimer();
        animateGrid();
        loadPlayerItems(); // Load available items into the Items Board
    }

    private void setDifficulty(String difficulty) {
        switch (difficulty) {
            case "medium":
                ROWS = 5;
                COLS = 9;
                NUM_MINES = 15;
                break;
            case "hard":
                ROWS = 6;
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
        bombsLeft = NUM_MINES; // Initialize bombs left
    }

    private void loadPlayerItems() {
        userRef.child("items").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                itemsBoard.removeAllViews(); // Clear existing buttons
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String itemName = itemSnapshot.getKey();
                    int itemCount = itemSnapshot.getValue(Integer.class);

                    if (itemCount > 0) {
                        addItemButton(itemName, itemCount);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load items.", Toast.LENGTH_SHORT).show();
        });
    }

    private void addItemButton(String itemName, int itemCount) {
        Button itemButton = new Button(this);
        itemButton.setText(itemName + " (" + itemCount + ")");
        itemButton.setOnClickListener(v -> useItem(itemName));
        itemButton.setBackgroundResource(R.drawable.item_button);
        itemsBoard.addView(itemButton);
    }

    private void useItem(String itemName) {
        switch (itemName) {
            case "hint":
                useHintItem();
                break;
            default:
                Toast.makeText(this, "Unknown item: " + itemName, Toast.LENGTH_SHORT).show();
        }
    }

    private void useHintItem() {
        boolean hintUsed = HintItem.useHint(board, buttons); // Hint logic encapsulated in HintItem
        if (hintUsed) {
            userRef.child("items").child("hint").get().addOnSuccessListener(snapshot -> {
                int currentCount = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                userRef.child("items").child("hint").setValue(currentCount - 1)
                        .addOnSuccessListener(aVoid -> loadPlayerItems());
            });
        } else {
            Toast.makeText(this, "No valid hint location available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateButtonClick(View button) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.1f, 1f);
        animator.setDuration(150);
        animator.start();

        ObjectAnimator animatorY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.1f, 1f);
        animatorY.setDuration(150);
        animatorY.start();
    }

    private void fetchUserPoints() {
        userRef.child("points").get()
                .addOnSuccessListener(snapshot -> {
                    int currentPoints = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                    pointsView.setText("Points: " + currentPoints);
                })
                .addOnFailureListener(e -> {
                    pointsView.setText("Points: --");
                    Toast.makeText(this, "Failed to fetch points: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchPersonalBest() {
        userRef.child("personalBests").child(difficulty).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        long bestTime = snapshot.getValue(Long.class);
                        personalBestView.setText("Best: " + bestTime + "s");
                    } else {
                        personalBestView.setText("Best: --");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load personal best.", Toast.LENGTH_SHORT).show());
    }

    private void updatePersonalBest() {
        userRef.child("personalBests").child(difficulty).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists() || timeElapsed < snapshot.getValue(Long.class)) {
                        userRef.child("personalBests").child(difficulty).setValue(timeElapsed);
                        personalBestView.setText("Best: " + timeElapsed + "s");
                        Toast.makeText(this, "New Personal Best!", Toast.LENGTH_SHORT).show();
                    }
                });
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

    private void animateGrid() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000); // Animation duration in milliseconds
        gridLayout.startAnimation(fadeIn);
    }


    private void setupGrid() {
        gridLayout.removeAllViews();
        gridLayout.setRowCount(ROWS);
        gridLayout.setColumnCount(COLS);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        int gridSize = Math.min(screenWidth, screenHeight) - 430;
        int tileSize = gridSize / Math.min(ROWS, COLS);

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Button cellButton = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);
                params.width = tileSize;
                params.height = tileSize;

                cellButton.setLayoutParams(params);

                // Set the initial texture to a blank tile
                cellButton.setBackgroundResource(R.drawable.blank_texture);

                int finalI = i;
                int finalJ = j;
                cellButton.setOnClickListener(v -> {
                    if (board[finalI][finalJ].isRevealed) {
                        handleRevealedCellClick(finalI, finalJ);
                    } else {
                        revealCell(finalI, finalJ);
                    }
                });
                cellButton.setOnLongClickListener(v -> {
                    flagCell(finalI, finalJ);
                    return true;
                });

                gridLayout.addView(cellButton);
                buttons[i][j] = cellButton;
            }
        }
        bombCounterView.setText("Bombs Left: " + bombsLeft);
    }


    private void handleRevealedCellClick(int row, int col) {
        int flaggedCount = countFlaggedAdjacentCells(row, col);

        if (flaggedCount == board[row][col].adjacentMines) {
            revealAllAdjacentTiles(row, col);
        }
    }

    private int countFlaggedAdjacentCells(int row, int col) {
        int count = 0;
        int[] directions = {-1, 0, 1};

        for (int dr : directions) {
            for (int dc : directions) {
                int newRow = row + dr;
                int newCol = col + dc;

                if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS) {
                    if (board[newRow][newCol].isFlagged) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void revealAllAdjacentTiles(int row, int col) {
        int[] directions = {-1, 0, 1};

        for (int dr : directions) {
            for (int dc : directions) {
                int newRow = row + dr;
                int newCol = col + dc;

                if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS) {
                    if (!board[newRow][newCol].isRevealed && !board[newRow][newCol].isFlagged) {
                        revealCell(newRow, newCol);
                    }
                }
            }
        }
    }

    private Drawable getNumberTexture(int number) {
        switch (number) {
            case 1:
                return getDrawable(R.drawable.one_texture);
            case 2:
                return getDrawable(R.drawable.two_texture);
            case 3:
                return getDrawable(R.drawable.three_texture);
            case 4:
                return getDrawable(R.drawable.four_texture);
            case 5:
                return getDrawable(R.drawable.five_texture);
            case 6:
                return getDrawable(R.drawable.six_texture);
            case 7:
                return getDrawable(R.drawable.seven_texture);
            case 8:
                return getDrawable(R.drawable.eight_texture);
            default:
                return getDrawable(R.drawable.empty_texture);
        }
    }

    private void revealCell(int row, int col) {
        if (!gameRunning || board[row][col].isRevealed || board[row][col].isFlagged) return;

        board[row][col].isRevealed = true;
        buttons[row][col].setEnabled(false);

        if (board[row][col].isMine) {
            gameRunning = false;
            Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show();
            revealAllMines();
            stopTimer();
            recordGameResult(false);
            return;
        }

        if (board[row][col].adjacentMines == 0) {
            buttons[row][col].setBackground(getDrawable(R.drawable.blank_texture));
            revealAdjacentCells(row, col);
        } else {
            buttons[row][col].setBackground(getNumberTexture(board[row][col].adjacentMines));
        }

        if (checkWinCondition()) {
            gameRunning = false;
            Toast.makeText(this, "You Win!", Toast.LENGTH_SHORT).show();
            stopTimer();
            recordGameResult(true);
            updatePersonalBest();
        }
    }



    private void revealAdjacentCells(int row, int col) {
        int[] directions = {-1, 0, 1};

        for (int dr : directions) {
            for (int dc : directions) {
                int newRow = row + dr;
                int newCol = col + dc;

                if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS) {
                    if (!board[newRow][newCol].isRevealed && !board[newRow][newCol].isMine) {
                        revealCell(newRow, newCol);
                    }
                }
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

    private void revealAllMines() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (board[i][j].isMine) {
                    buttons[i][j].setBackground(getDrawable(R.drawable.bomb_texture));
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
        fetchPersonalBest();
        startTimer();
        bombsLeft = NUM_MINES;
    }

    private void recordGameResult(boolean won) {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail()
                : "guest";

        if (userEmail != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("gameHistory");

            HashMap<String, Object> gameData = new HashMap<>();
            gameData.put("won", won);
            gameData.put("timeTaken", timeElapsed);
            gameData.put("difficulty", difficulty);
            gameData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date()));

            dbRef.child(userEmail.replace(".", ",")).push().setValue(gameData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(GameActivity.this, "Game result saved!", Toast.LENGTH_SHORT).show();

                        if (won) {
                            updatePoints(userEmail);
                            updateLeaderboard(userEmail);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(GameActivity.this, "Failed to save game result: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updatePoints(String userEmail) {
        userRef.child("points").get().addOnSuccessListener(snapshot -> {
            int currentPoints = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
            int newPoints = calculatePoints();
            int updatedPoints = currentPoints + newPoints;

            userRef.child("points").setValue(updatedPoints)
                    .addOnSuccessListener(aVoid -> pointsView.setText("Points: " + updatedPoints))
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update points: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private int calculatePoints() {
        int basePoints;
        switch (difficulty) {
            case "medium":
                basePoints = 2000;
                return Math.max(0, (basePoints - timeElapsed) * 2);
            case "hard":
                basePoints = 3000;
                return Math.max(0, (basePoints - timeElapsed) * 3);
            case "easy":
            default:
                basePoints = 1000;
                return Math.max(0, (basePoints - timeElapsed));
        }
    }

    private void updateLeaderboard(String userEmail) {
        HashMap<String, Object> leaderboardData = new HashMap<>();
        leaderboardData.put("email", userEmail);
        leaderboardData.put("time", timeElapsed);

        leaderboardRef.child(difficulty)
                .child(userEmail.replace(".", ","))
                .setValue(leaderboardData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Leaderboard updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update leaderboard: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void flagCell(int row, int col) {
        if (!gameRunning || board[row][col].isRevealed) return;

        board[row][col].isFlagged = !board[row][col].isFlagged;

        if (board[row][col].isFlagged) {
            // Set the background to flag texture when flagged
            buttons[row][col].setBackgroundResource(R.drawable.flag_texture);
            bombsLeft--;
        } else {
            // Reset to default tile background when unflagged
            buttons[row][col].setBackgroundResource(R.drawable.blank_texture);
            bombsLeft++;
        }

        // Update the bomb counter UI
        bombCounterView.setText("Bombs Left: " + bombsLeft);
    }

}
