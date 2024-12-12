package com.example.minesweeperhansasattochna;

import android.graphics.Color;
import android.os.Handler;
import android.widget.Button;

public class MineDetectorItem {
    public static void revealMinesTemporarily(Cell[][] board, Button[][] buttons, int durationMillis) {
        // Highlight all mines
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].isMine) {
                    buttons[i][j].setBackgroundColor(Color.RED);
                }
            }
        }

        // Reset after the duration
        new Handler().postDelayed(() -> {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j].isMine) {
                        buttons[i][j].setBackgroundResource(R.drawable.deafault_texture);
                    }
                }
            }
        }, durationMillis);
    }
}
