package com.superhellth.utils;

public class BitboardUtils {

    public static int[] getPopulatedIndices(long bitboard) {
        int[] indices = new int[Long.bitCount(bitboard)];
        int i = 0;
        while (bitboard != 0) {
            indices[i++] = Long.numberOfTrailingZeros(bitboard);
            bitboard &= bitboard - 1;
        }
        return indices;
    }

    public static long shiftNorth(long bitboard, int n) {
        return bitboard << 8 * n;
    }

    public static long shiftSouth(long bitboard, int n) {
        return bitboard >>> 8 * n;
    }

}
