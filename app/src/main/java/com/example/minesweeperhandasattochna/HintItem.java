package com.example.minesweeperhandasattochna;

import android.widget.Button;

public class HintItem {
    public static boolean useHint(Cell[][] board, Button[][] buttons, boolean gameRunning) {
        if(gameRunning)
        {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j].isMine && !board[i][j].isFlagged) {
                        // Place a flag on the first unflagged mine
                        buttons[i][j].setBackgroundResource(R.drawable.flag_texture);
                        board[i][j].isFlagged = true;
                        return true; // Successfully used the hint
                    }
                }
            }
            return false; // No valid hint location fou
        }

        return false;
    }
}
