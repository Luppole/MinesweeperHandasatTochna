package com.example.minesweeperhansasattochna;

public class Cell {
    boolean isMine;
    boolean isRevealed;
    boolean isFlagged;
    int adjacentMines;

    public Cell() {
        isMine = false;
        isRevealed = false;
        isFlagged = false;
        adjacentMines = 0;
    }
}
