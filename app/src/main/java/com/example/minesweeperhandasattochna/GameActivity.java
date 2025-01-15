package com.example.minesweeperhandasattochna;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.speech.tts.TextToSpeech;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements SensorEventListener {

    private Cell[][] board;
    private int ROWS, COLS, NUM_MINES;
    private GridLayout gridLayout;
    private TextView timer, personalBestView, bombCounterView, pointsView;
    private Button[][] buttons;
    private ImageButton homeButton;
    private Handler timerHandler = new Handler();
    private boolean gameRunning = false;
    private int timeElapsed = 0;
    private int bombsLeft, userPoints;
    private NotificationService notificationService; // Add NotificationService as a member variable
    private DatabaseReference userRef, leaderboardRef;
    private String difficulty;
    private MediaPlayer explosionSound;
    private SensorManager sensorManager;
    private TextToSpeech ttsClient;
    private String ttsText;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 12.0f;
    private long lastShakeTime = 0;

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
        Button inventoryButton = findViewById(R.id.inventoryButton);
        homeButton = findViewById(R.id.homeButton);
        Button leaderboardButton = findViewById(R.id.leaderboardButton);
        Button gameHistoryButton = findViewById(R.id.gameHistoryButton);
        Button resetButton = findViewById(R.id.resetButton);

        ttsClient = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR)
                    ttsClient.setLanguage(Locale.ENGLISH);
            }
        });

        // Get difficulty level
        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = "easy";
        }

        ttsText = "Difficulty chosen was " + difficulty;

        ttsClient.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null);
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

        // Initialize SensorManager and Accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast.makeText(this, "Accelerometer not available", Toast.LENGTH_SHORT).show();
            }
        }

        inventoryButton.setOnClickListener(v -> showInventoryPopup());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        boolean isAuthenticated = mAuth.getCurrentUser() != null;

        if (isAuthenticated) {
            gameHistoryButton.setEnabled(true); // Enable button
            gameHistoryButton.setOnClickListener(v -> {
                animateButtonClick(v); // Animate button click
                Intent historyIntent = new Intent(GameActivity.this, GameHistoryActivity.class);
                startActivity(historyIntent);
            });
        } else {
            gameHistoryButton.setEnabled(false); // Disable button for guests
            gameHistoryButton.setAlpha(0.5f); // Dim the button to visually indicate it's disabled
            gameHistoryButton.setOnClickListener(v -> {
                Toast.makeText(this, "Game history is available only for logged-in users.", Toast.LENGTH_SHORT).show();
            });
        }

        resetButton.setOnClickListener(v -> {
            animateButtonClick(v); // Animate button click
            resetGame();
        });

        // Notification Service Initialization
        notificationService = new NotificationService(this);

        // Other initializations
        setDifficulty(difficulty);
        fetchPersonalBest();
        initializeBoard();
        setupGrid();
        startTimer();
        animateGrid();
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
            if (acceleration > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShakeTime > 1000) { // Prevent multiple triggers
                    lastShakeTime = currentTime;
                    onShakeDetected();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used for this implementation
    }

    private void onShakeDetected() {
        // Action to open inventory
        showInventoryPopup();
    }


    private void fetchUserPoints() {
        userRef.child("points").get()
                .addOnSuccessListener(snapshot -> {
                    userPoints = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                    pointsView.setText("Points: " + userPoints);
                })
                .addOnFailureListener(e -> {
                    pointsView.setText("Points: --");
                    Toast.makeText(this, "Playing as a guest", Toast.LENGTH_SHORT).show();
                });
    }

    private void showInventoryPopup() {
        // Inflate the inventory popup layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_inventory_popup, null);

        // Create the popup window
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(10);

        // Set up buttons
        Button useHintButton = popupView.findViewById(R.id.useHintButton);
        Button useSuperHintButton = popupView.findViewById(R.id.useSuperHintButton);
        Button useShieldButton = popupView.findViewById(R.id.useShieldButton);
        Button useMineDetectorButton = popupView.findViewById(R.id.useMineDetectorButton);
        Button closeButton = popupView.findViewById(R.id.closeButton);

        // Load inventory items and enable/disable buttons accordingly
        setupInventoryButton(userRef, "hint", useHintButton, () -> useHintItem(popupWindow));
        setupInventoryButton(userRef, "superHint", useSuperHintButton, () -> useSuperHintItem(popupWindow));
        setupInventoryButton(userRef, "shield", useShieldButton, () -> useShieldItem(popupWindow));
        setupInventoryButton(userRef, "mineDetector", useMineDetectorButton, () -> useMineDetectorItem(popupWindow));

        // Close button logic
        closeButton.setOnClickListener(v -> popupWindow.dismiss());

        // Add drag functionality
        popupView.setOnTouchListener(new View.OnTouchListener() {
            private float initialX, initialY;
            private float offsetX, offsetY;
            private boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getRawX();
                        initialY = event.getRawY();
                        offsetX = event.getRawX() - popupWindow.getContentView().getLeft();
                        offsetY = event.getRawY() - popupWindow.getContentView().getTop();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - initialX;
                        float deltaY = event.getRawY() - initialY;

                        if (!isDragging && (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10)) {
                            isDragging = true;
                        }

                        if (isDragging) {
                            popupWindow.update((int) (event.getRawX() - offsetX), (int) (event.getRawY() - offsetY), -1, -1);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            v.performClick(); // Pass click to child views
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });

        // Show the popup window at the default position
        popupWindow.showAtLocation(gridLayout, Gravity.NO_GRAVITY, 50, 200);
    }

    // Helper function to configure inventory button
    private void setupInventoryButton(DatabaseReference userRef, String itemName, Button button, Runnable onUseAction) {
        userRef.child("items").child(itemName).get().addOnSuccessListener(snapshot -> {
            int itemCount = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
            button.setText(itemName + " (" + itemCount + ")");
            button.setEnabled(itemCount > 0);
        }).addOnFailureListener(e -> {
            button.setText(itemName + " (--)");
            button.setEnabled(false);
        });

        button.setOnClickListener(v -> onUseAction.run());
    }

    private void showPopupMessage(String message) {
        // Inflate the popup layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_popup_message, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));


        // Set the message
        TextView popupMessage = popupView.findViewById(R.id.popupMessage);
        popupMessage.setText(message);

        // Add the popup to the root layout
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(10);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        // Start fade-out animation
        popupView.clearAnimation();

        // Dismiss popup after animation
        popupView.postDelayed(popupWindow::dismiss, 3000); // 3 secs

        ttsText = message;
        ttsClient.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void useHintItem(PopupWindow popupWindow) {
        boolean hintUsed = HintItem.useHint(board, buttons, gameRunning); // Hint logic encapsulated in HintItem
        if (hintUsed) {
            userRef.child("items").child("hint").get().addOnSuccessListener(snapshot -> {
                int currentCount = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                if (currentCount > 0) {
                    userRef.child("items").child("hint").setValue(currentCount - 1)
                            .addOnSuccessListener(aVoid -> {
                                showPopupMessage("Hint Used!");
                                popupWindow.dismiss(); // Close the inventory after using the item
                            });
                }
            });
        }
    }



    private void useSuperHintItem(PopupWindow popupWindow) {
        userRef.child("items").child("superHint").get().addOnSuccessListener(snapshot -> {
            int currentCount = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
            if (currentCount > 0) {
                boolean superHintUsed = SuperHintItem.useSuperHint(board, buttons, gameRunning);
                if (superHintUsed) {
                    userRef.child("items").child("superHint").setValue(currentCount - 1)
                            .addOnSuccessListener(aVoid -> {
                                showPopupMessage("Super Hint Used!");
                                popupWindow.dismiss(); // Close the inventory
                            });
                }
            }
        });
    }

    private boolean shieldActive = false;

    public void setShieldActive(boolean isActive) {
        shieldActive = isActive;
    }

    private void useShieldItem(PopupWindow popupWindow) {
        userRef.child("items").child("shield").get().addOnSuccessListener(snapshot -> {
            int currentCount = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
            if (currentCount > 0 && gameRunning) {
                ShieldItem.activateShield(this); // Activate shield logic
                userRef.child("items").child("shield").setValue(currentCount - 1)
                        .addOnSuccessListener(aVoid -> {
                            showPopupMessage("Shield Used!");
                            popupWindow.dismiss(); // Close inventory popup
                        });
            }
        });
    }

    private void useMineDetectorItem(PopupWindow popupWindow) {
        userRef.child("items").child("mineDetector").get().addOnSuccessListener(snapshot -> {
            int currentCount = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
            if (currentCount > 0 && gameRunning) {
                MineDetectorItem.revealMinesTemporarily(board, buttons, 2000); // Reveal mines for 2 seconds
                userRef.child("items").child("mineDetector").setValue(currentCount - 1)
                        .addOnSuccessListener(aVoid -> {
                            showPopupMessage("Mine Detector Used!");
                            popupWindow.dismiss(); // Close inventory popup
                        });
            }
        });

    }

    private void playExplosionSound() {
        if (explosionSound == null) {
            explosionSound = MediaPlayer.create(this, R.raw.explosion); // Ensure your sound file is in res/raw
        }
        explosionSound.start();
    }



    private void animateButtonClick(View button) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.1f, 1f);
        animator.setDuration(150);
        animator.start();

        ObjectAnimator animatorY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.1f, 1f);
        animatorY.setDuration(150);
        animatorY.start();
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
                });}

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
                cellButton.setBackgroundResource(R.drawable.deafault_texture);

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
                return getDrawable(R.drawable.blank_texture);
        }
    }

    private void showWinPopup() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.activity_popup_win, null);
        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.showAtLocation(gridLayout, Gravity.CENTER, 0, 0);

        new Handler().postDelayed(popupWindow::dismiss, 2000); // Auto-dismiss after 2 seconds
    }

    private void showLossPopup() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.activity_popup_loss, null);
        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.showAtLocation(gridLayout, Gravity.CENTER, 0, 0);

        new Handler().postDelayed(popupWindow::dismiss, 2000); // Auto-dismiss after 2 seconds
    }


    private void revealCell(int row, int col) {
        if (!gameRunning || board[row][col].isRevealed || board[row][col].isFlagged) return;

        board[row][col].isRevealed = true;
        buttons[row][col].setEnabled(false);

        if (board[row][col].isMine) {
            if (shieldActive) { // Check if the shield is active
                shieldActive = false; // Deactivate the shield after use
                board[row][col].isRevealed = false; // Prevent marking this cell as revealed
                buttons[row][col].setEnabled(true); // Re-enable the button
                Toast.makeText(this, "Shield activated! You are saved from an explosion.", Toast.LENGTH_SHORT).show();
                return; // Prevent the game from ending
            } else {
                playExplosionSound(); // Play the explosion sound
                showLossPopup();
                gameRunning = false;
                notificationService.sendNotification(
                        "Defeat!",
                        "You lost the game. Try again!",
                        GameActivity.class
                );                revealAllMines();
                stopTimer();
                recordGameResult(false);
                return;
            }
        }

        if (board[row][col].adjacentMines == 0) {
            buttons[row][col].setBackground(getDrawable(R.drawable.blank_texture));
            revealAdjacentCells(row, col);
        } else {
            buttons[row][col].setBackground(getNumberTexture(board[row][col].adjacentMines));
        }

        if (checkWinCondition()) {
            gameRunning = false;
            showWinPopup(); // Show the WIN popup
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
            DatabaseReference userRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("users")
                    .child(userEmail.replace(".", ","));

            // Save game history
            HashMap<String, Object> gameData = new HashMap<>();
            gameData.put("won", won);
            gameData.put("timeTaken", timeElapsed);
            gameData.put("difficulty", difficulty);
            gameData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date()));

            userRef.child("gameHistory").push().setValue(gameData)
                    .addOnSuccessListener(aVoid -> {
                        // Update statistics, points, and leaderboard
                        updateStatistics(userRef, won);
                        if (won) {
                            notificationService.sendNotification(
                                    "Victory!",
                                    "You won the game! Play again to keep winning!",
                                    GameActivity.class
                            );
                            updatePoints(userRef);
                            updateLeaderboard(userEmail);
                        }
                        else {
                            NotificationService notificationService = new NotificationService(this);
                            notificationService.sendNotification(
                                    "Defeat!",
                                    "You lost the game. Try again!",
                                    GameActivity.class
                            );
                        }
                    });
        }
    }

    private void updateAchievements(DatabaseReference userRef, int totalGames, int totalWins, int winStreak) {
        userRef.child("achievements").get().addOnSuccessListener(snapshot -> {
            HashMap<String, Object> achievementsUpdate = new HashMap<>();

            // Update "Play 100 Games" achievement
            if (snapshot.child("play_100_games/progress").exists()) {
                int currentProgress = snapshot.child("play_100_games/progress").getValue(Integer.class);
                int goal = snapshot.child("play_100_games/goal").getValue(Integer.class);
                int newProgress = Math.min(totalGames, goal); // Ensure progress doesn't exceed the goal
                achievementsUpdate.put("play_100_games/progress", newProgress);
            }

            // Update "Win 50 Games" achievement
            if (snapshot.child("win_50_games/progress").exists()) {
                int currentProgress = snapshot.child("win_50_games/progress").getValue(Integer.class);
                int goal = snapshot.child("win_50_games/goal").getValue(Integer.class);
                int newProgress = Math.min(totalWins, goal);
                achievementsUpdate.put("win_50_games/progress", newProgress);
            }

            // Update "Win Streak of 10 Games" achievement
            if (snapshot.child("win_streak_10/progress").exists()) {
                int currentProgress = snapshot.child("win_streak_10/progress").getValue(Integer.class);
                int goal = snapshot.child("win_streak_10/goal").getValue(Integer.class);
                int newProgress = Math.min(winStreak, goal);
                achievementsUpdate.put("win_streak_10/progress", newProgress);
            }

            // Push updated achievements to Firebase
            userRef.child("achievements").updateChildren(achievementsUpdate);
        });
    }


    private void updateStatistics(DatabaseReference userRef, boolean won) {
        userRef.child("stats").get().addOnSuccessListener(snapshot -> {
            int totalWins = snapshot.child("totalWins").exists() ? snapshot.child("totalWins").getValue(Integer.class) : 0;
            int totalLosses = snapshot.child("totalLosses").exists() ? snapshot.child("totalLosses").getValue(Integer.class) : 0;
            int winStreak = snapshot.child("winStreak").exists() ? snapshot.child("winStreak").getValue(Integer.class) : 0;
            int longestStreak = snapshot.child("longestStreak").exists() ? snapshot.child("longestStreak").getValue(Integer.class) : 0;
            int totalGames = totalWins + totalLosses;

            if (won) {
                totalWins++;
                winStreak++;
                longestStreak = Math.max(longestStreak, winStreak);
            } else {
                totalLosses++;
                winStreak = 0; // Reset win streak on loss
            }

            totalGames++; // Increment total games for every game played

            // Update achievements
            updateAchievements(userRef, totalGames, totalWins, winStreak);

            // Save updated stats in Firebase
            HashMap<String, Object> statsUpdate = new HashMap<>();
            statsUpdate.put("totalWins", totalWins);
            statsUpdate.put("totalLosses", totalLosses);
            statsUpdate.put("totalGames", totalGames); // Ensure total games is updated
            statsUpdate.put("winStreak", winStreak);
            statsUpdate.put("longestStreak", longestStreak);

            userRef.child("stats").updateChildren(statsUpdate);
        });
    }



    private void updatePoints(DatabaseReference userRef) {
        userRef.child("points").get().addOnSuccessListener(snapshot -> {
            int currentPoints = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
            int pointsEarned = calculatePoints();
            int updatedPoints = currentPoints + pointsEarned;

            userRef.child("points").setValue(updatedPoints);
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
        DatabaseReference leaderboardRef = FirebaseDatabase.getInstance("https://minesweeperhandasattochna-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("leaderboard")
                .child(difficulty);

        HashMap<String, Object> leaderboardData = new HashMap<>();
        leaderboardData.put("email", userEmail);
        leaderboardData.put("time", timeElapsed);

        leaderboardRef.child(userEmail.replace(".", ","))
                .setValue(leaderboardData);
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
            buttons[row][col].setBackgroundResource(R.drawable.deafault_texture);
            bombsLeft++;
        }

        // Update the bomb counter UI
        bombCounterView.setText("Bombs Left: " + bombsLeft);
    }

}
