package com.superhellth.utils;

public class BoardUtils {

    public static boolean isLightSquare(int file, int rank) {
        return (file + rank) % 2 == 0;
    }

    public static int[] getRankAndFileFromSquareIndex(int squareIndex) {
        int file = squareIndex % 8;
        int rank = 7 - squareIndex / 8;
        return new int[]{file, rank};
    }

}
