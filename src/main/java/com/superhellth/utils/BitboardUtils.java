package com.superhellth.utils;

import com.superhellth.basics.Direction;

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

    public static long shift(long bitboard, Direction direction) {
        return switch (direction) {
            case NORTH ->
                bitboard << 8;
            case SOUTH ->
                bitboard >>> 8;
            case EAST ->
                bitboard << 1 & 0xFEFEFEFEFEFEFEFEL;
            case WEST ->
                bitboard >>> 1 & 0x7F7F7F7F7F7F7F7FL;
            case NORTH_EAST ->
                (bitboard << 9) & 0xFEFEFEFEFEFEFEFEL;
            case NORTH_WEST ->
                (bitboard << 7) & 0x7F7F7F7F7F7F7F7FL;
            case SOUTH_EAST ->
                (bitboard >>> 7) & 0xFEFEFEFEFEFEFEFEL;
            case SOUTH_WEST ->
                (bitboard >>> 9) & 0x7F7F7F7F7F7F7F7FL;
        };
    }

    public static long flood(long population, long empty, Direction direction) {
        // call by ref / value?
        long flood = 0L;
        while (population != 0) {
            flood |= population;
            population = (population >> 8) & empty;
        }
        return flood;
    }

}
