package com.superhellth.utils;

public class BoardUtils {

    public static boolean isLightSquare(int file, int rank) {
        return (file + rank) % 2 == 0;
    }

    public static int[] getPopulatedIndices(long bitboard) {
        int[] indices = new int[Long.bitCount(bitboard)];
        int i = 0;
        while (bitboard != 0) {
            indices[i++] = Long.numberOfTrailingZeros(bitboard);
            bitboard &= bitboard - 1;
        }
        return indices;
    }

    public static int[] getRankAndFileFromSquareIndex(int squareIndex) {
        int file = squareIndex % 8;
        int rank = squareIndex / 8;
        return new int[]{file, rank};
    }

}
