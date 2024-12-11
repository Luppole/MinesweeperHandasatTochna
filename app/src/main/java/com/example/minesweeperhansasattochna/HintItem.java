package com.example.minesweeperhansasattochna;

import android.widget.Button;

public class HintItem {
    public static boolean useHint(Cell[][] board, Button[][] buttons) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].isMine && !board[i][j].isFlagged) {
                    board[i][j].isFlagged = true;
                    buttons[i][j].setBackgroundResource(R.drawable.flag_texture); // Use flag texture
                    return true;
                }
            }
        }
        return false; // No valid hint location
    }
}
