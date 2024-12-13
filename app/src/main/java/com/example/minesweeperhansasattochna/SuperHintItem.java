package com.example.minesweeperhansasattochna;

import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SuperHintItem {
    public static boolean useSuperHint(Cell[][] board, Button[][] buttons) {
        List<int[]> nonBombTiles = new ArrayList<>();

        // Collect all non-bomb tile coordinates
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (!board[i][j].isMine && !board[i][j].isRevealed) {
                    nonBombTiles.add(new int[]{i, j});
                }
            }
        }

        // If there are fewer than 5 tiles to reveal, return false
        if (nonBombTiles.size() < 5) return false;

        // Shuffle and pick 5 random tiles
        Collections.shuffle(nonBombTiles);
        for (int k = 0; k < 5; k++) {
            int[] tile = nonBombTiles.get(k);
            int row = tile[0];
            int col = tile[1];

            // Reveal the selected tile
            board[row][col].isRevealed = true;
            buttons[row][col].setEnabled(false);

            if (board[row][col].adjacentMines == 0) {
                buttons[row][col].setBackgroundResource(R.drawable.blank_texture);
            } else {
                buttons[row][col].setBackgroundResource(getNumberTexture(board[row][col].adjacentMines));
            }
        }

        return true; // Super Hint successfully used
    }

    private static int getNumberTexture(int number) {
        switch (number) {
            case 1:
                return R.drawable.one_texture;
            case 2:
                return R.drawable.two_texture;
            case 3:
                return R.drawable.three_texture;
            case 4:
                return R.drawable.four_texture;
            case 5:
                return R.drawable.five_texture;
            case 6:
                return R.drawable.six_texture;
            case 7:
                return R.drawable.seven_texture;
            case 8:
                return R.drawable.eight_texture;
            default:
                return R.drawable.blank_texture;
        }
    }
}
