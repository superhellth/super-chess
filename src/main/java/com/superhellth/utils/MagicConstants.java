package com.superhellth.utils;

public class MagicConstants {

    public static final long[] ROOK_MASKS = new long[64];
    public static final long[][] ROOK_TABLE = new long[64][];

    public static final int[] ROOK_SHIFTS = {
        12, 11, 11, 11, 11, 11, 11, 12,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        12, 11, 11, 11, 11, 11, 11, 12
    };

    public static final long[] ROOK_MAGICS = {
        0x8a80104000800020L, 0x140002000100040L, 0x2801880a0017001L, 0x100081001000420L, 0x200020010080420L,
        0x3001c0002010008L, 0x8480008002000100L, 0x2080088004402900L, 0x800098204000L, 0x2024401000200040L,
        0x100802000801000L, 0x120800800801000L, 0x208808088000400L, 0x2802200800400L, 0x2200800100020080L,
        0x801000060821100L, 0x80044006422000L, 0x100808020004000L, 0x12108a0010204200L, 0x140848010000802L,
        0x481828014002800L, 0x8094004002004100L, 0x4010040010010802L, 0x20008806104L, 0x100400080208000L,
        0x2040002120081000L,
        0x21200680100081L,
        0x20100080080080L,
        0x2000a00200410L,
        0x20080800400L,
        0x80088400100102L,
        0x80004600042881L,
        0x4040008040800020L,
        0x440003000200801L,
        0x4200011004500L,
        0x188020010100100L,
        0x14800401802800L,
        0x2080040080800200L,
        0x124080204001001L,
        0x200046502000484L,
        0x480400080088020L,
        0x1000422010034000L,
        0x30200100110040L,
        0x100021010009L,
        0x2002080100110004L,
        0x202008004008002L,
        0x20020004010100L,
        0x2048440040820001L,
        0x101002200408200L,
        0x40802000401080L,
        0x4008142004410100L,
        0x2060820c0120200L,
        0x1001004080100L,
        0x20c020080040080L,
        0x2935610830022400L,
        0x44440041009200L,
        0x280001040802101L,
        0x2100190040002085L,
        0x80c0084100102001L,
        0x4024081001000421L,
        0x20030a0244872L,
        0x12001008414402L,
        0x2006104900a0804L,
        0x1004081002402L
    };

    static {
        for (int sq = 0; sq < 64; sq++) {
            ROOK_MASKS[sq] = computeRookMask(sq);

            int numBits = ROOK_SHIFTS[sq];
            ROOK_TABLE[sq] = new long[1 << numBits];

            long[] blockerConfigs = enumerateBlockerConfigs(ROOK_MASKS[sq]);
            for (long blockers : blockerConfigs) {
                int index = (int) ((blockers * ROOK_MAGICS[sq]) >>> (64 - numBits));
                ROOK_TABLE[sq][index] = computeRookAttacks(sq, blockers);
            }
        }
    }

    private static long computeRookMask(int square) {
        long mask = 0L;
        int rank = square / 8;
        int file = square % 8;

        // North (exclude rank 7)
        for (int r = rank + 1; r < 7; r++) {
            mask |= 1L << (r * 8 + file);
        }
        // South (exclude rank 0)
        for (int r = rank - 1; r > 0; r--) {
            mask |= 1L << (r * 8 + file);
        }
        // East (exclude file 7)
        for (int f = file + 1; f < 7; f++) {
            mask |= 1L << (rank * 8 + f);
        }
        // West (exclude file 0)
        for (int f = file - 1; f > 0; f--) {
            mask |= 1L << (rank * 8 + f);
        }

        return mask;
    }

    private static long[] enumerateBlockerConfigs(long mask) {
        int numBits = Long.bitCount(mask);
        int numConfigs = 1 << numBits;
        long[] configs = new long[numConfigs];

        // Map each integer 0..2^n-1 to a subset of mask
        // by distributing its bits across the set bits of mask
        int[] maskBitIndices = new int[numBits];
        long temp = mask;
        for (int i = 0; i < numBits; i++) {
            maskBitIndices[i] = Long.numberOfTrailingZeros(temp);
            temp &= temp - 1;
        }

        for (int i = 0; i < numConfigs; i++) {
            long config = 0L;
            for (int bit = 0; bit < numBits; bit++) {
                if ((i & (1 << bit)) != 0) {
                    config |= 1L << maskBitIndices[bit];
                }
            }
            configs[i] = config;
        }
        return configs;
    }

    private static long computeRookAttacks(int square, long blockers) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        // North
        for (int r = rank + 1; r <= 7; r++) {
            long sq = 1L << (r * 8 + file);
            attacks |= sq;
            if ((blockers & sq) != 0) {
                break;
            }
        }
        // South
        for (int r = rank - 1; r >= 0; r--) {
            long sq = 1L << (r * 8 + file);
            attacks |= sq;
            if ((blockers & sq) != 0) {
                break;
            }
        }
        // East
        for (int f = file + 1; f <= 7; f++) {
            long sq = 1L << (rank * 8 + f);
            attacks |= sq;
            if ((blockers & sq) != 0) {
                break;
            }
        }
        // West
        for (int f = file - 1; f >= 0; f--) {
            long sq = 1L << (rank * 8 + f);
            attacks |= sq;
            if ((blockers & sq) != 0) {
                break;
            }
        }
        return attacks;
    }
}
