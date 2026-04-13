package com.superhellth.utils;

import com.superhellth.basics.Color;

public class BoardUtils {

    public static boolean isLightSquare(int squareIndex) {
        return ((squareIndex / 8) + (squareIndex % 8)) % 2 == 0;
    }

    public static int[] getRankAndFileFromSquareIndex(int squareIndex) {
        int file = squareIndex % 8;
        int rank = squareIndex / 8;
        return new int[]{rank, file};
    }

    public static int getSquareIndexFromRankAndFile(int rank, int file) {
        return rank * 8 + file;
    }

    public static Color getOppositeColor(Color color) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when getting opposite color";
        if (color == Color.WHITE) {
            return Color.BLACK;
        }
        return Color.WHITE;
    }

}
