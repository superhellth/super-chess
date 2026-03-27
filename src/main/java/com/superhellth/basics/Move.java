package com.superhellth.basics;

public class Move {
    private final int fromSquare;
    private final int toSquare;

    public Move(int fromSquare, int toSquare) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
    }

    public int getFromSquare() {
        return this.fromSquare;
    }

    public int getToSquare() {
        return this.toSquare;
    }
}