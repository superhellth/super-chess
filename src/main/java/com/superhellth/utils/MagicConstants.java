package com.superhellth.utils;

public class MagicConstants {

    public static final long[] ROOK_MASKS = new long[64];
    public static final long[] BISHOP_MASKS = new long[64];
    public static final long[][] ROOK_TABLE = new long[64][];
    public static final long[][] BISHOP_TABLE = new long[64][];

    // Ray masks for each square in each direction (indexed by Direction ordinal)
    // NORTH=0, SOUTH=1, EAST=2, WEST=3, NORTH_WEST=4, NORTH_EAST=5, SOUTH_WEST=6, SOUTH_EAST=7
    public static final long[][] RAY_MASKS = new long[64][8];

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

    public static final int[] BISHOP_SHIFTS = {
        6, 5, 5, 5, 5, 5, 5, 6,
        5, 5, 5, 5, 5, 5, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 5, 5, 5, 5, 5, 5,
        6, 5, 5, 5, 5, 5, 5, 6
    };

    public static final long[] ROOK_MAGICS = {
        0x8a80104000800020L, 0x140002000100040L, 0x2801880a0017001L, 0x100081001000420L, 0x200020010080420L,
        0x3001c0002010008L, 0x8480008002000100L, 0x2080088004402900L, 0x800098204000L, 0x2024401000200040L,
        0x100802000801000L, 0x120800800801000L, 0x208808088000400L, 0x2802200800400L, 0x2200800100020080L,
        0x801000060821100L, 0x80044006422000L, 0x100808020004000L, 0x12108a0010204200L, 0x140848010000802L,
        0x481828014002800L, 0x8094004002004100L, 0x4010040010010802L, 0x20008806104L, 0x100400080208000L,
        0x2040002120081000L, 0x21200680100081L, 0x20100080080080L, 0x2000a00200410L, 0x20080800400L,
        0x80088400100102L, 0x80004600042881L, 0x4040008040800020L, 0x440003000200801L, 0x4200011004500L,
        0x188020010100100L, 0x14800401802800L, 0x2080040080800200L, 0x124080204001001L, 0x200046502000484L,
        0x480400080088020L, 0x1000422010034000L, 0x30200100110040L, 0x100021010009L, 0x2002080100110004L,
        0x202008004008002L, 0x20020004010100L, 0x2048440040820001L, 0x101002200408200L, 0x40802000401080L,
        0x4008142004410100L, 0x2060820c0120200L, 0x1001004080100L, 0x20c020080040080L, 0x2935610830022400L,
        0x44440041009200L, 0x280001040802101L, 0x2100190040002085L, 0x80c0084100102001L, 0x4024081001000421L,
        0x20030a0244872L, 0x12001008414402L, 0x2006104900a0804L, 0x1004081002402L
    };

    public static final long[] BISHOP_MAGICS = {
        0x40040844404084L,
        0x2004208a004208L,
        0x10190041080202L,
        0x108060845042010L,
        0x581104180800210L,
        0x2112080446200010L,
        0x1080820820060210L,
        0x3c0808410220200L,
        0x4050404440404L,
        0x21001420088L,
        0x24d0080801082102L,
        0x1020a0a020400L,
        0x40308200402L,
        0x4011002100800L,
        0x401484104104005L,
        0x801010402020200L,
        0x400210c3880100L,
        0x404022024108200L,
        0x810018200204102L,
        0x4002801a02003L,
        0x85040820080400L,
        0x810102c808880400L,
        0xe900410884800L,
        0x8002020480840102L,
        0x220200865090201L,
        0x2010100a02021202L,
        0x152048408022401L,
        0x20080002081110L,
        0x4001001021004000L,
        0x800040400a011002L,
        0xe4004081011002L,
        0x1c004001012080L,
        0x8004200962a00220L,
        0x8422100208500202L,
        0x2000402200300c08L,
        0x8646020080080080L,
        0x80020a0200100808L,
        0x2010004880111000L,
        0x623000a080011400L,
        0x42008c0340209202L,
        0x209188240001000L,
        0x400408a884001800L,
        0x110400a6080400L,
        0x1840060a44020800L,
        0x90080104000041L,
        0x201011000808101L,
        0x1a2208080504f080L,
        0x8012020600211212L,
        0x500861011240000L,
        0x180806108200800L,
        0x4000020e01040044L,
        0x300000261044000aL,
        0x802241102020002L,
        0x20906061210001L,
        0x5a84841004010310L,
        0x4010801011c04L,
        0xa010109502200L,
        0x4a02012000L,
        0x500201010098b028L,
        0x8040002811040900L,
        0x28000010020204L,
        0x6000020202d0240L,
        0x8918844842082200L,
        0x4010011029020020L
    };

    static {
        initializeMagicTable(ROOK_MASKS, ROOK_TABLE, ROOK_SHIFTS, ROOK_MAGICS,
                MagicConstants::computeRookMask, MagicConstants::computeRookAttacks);
        initializeMagicTable(BISHOP_MASKS, BISHOP_TABLE, BISHOP_SHIFTS, BISHOP_MAGICS,
                MagicConstants::computeBishopMask, MagicConstants::computeBishopAttacks);
        initializeRayMasks();
    }

    private static void initializeRayMasks() {
        for (int sq = 0; sq < 64; sq++) {
            int rank = sq / 8;
            int file = sq % 8;
            long ray;

            // NORTH (ordinal 0)
            ray = 0L;
            for (int r = rank + 1; r <= 7; r++) ray |= 1L << (r * 8 + file);
            RAY_MASKS[sq][0] = ray;

            // SOUTH (ordinal 1)
            ray = 0L;
            for (int r = rank - 1; r >= 0; r--) ray |= 1L << (r * 8 + file);
            RAY_MASKS[sq][1] = ray;

            // EAST (ordinal 2)
            ray = 0L;
            for (int f = file + 1; f <= 7; f++) ray |= 1L << (rank * 8 + f);
            RAY_MASKS[sq][2] = ray;

            // WEST (ordinal 3)
            ray = 0L;
            for (int f = file - 1; f >= 0; f--) ray |= 1L << (rank * 8 + f);
            RAY_MASKS[sq][3] = ray;

            // NORTH_WEST (ordinal 4)
            ray = 0L;
            for (int r = rank + 1, f = file - 1; r <= 7 && f >= 0; r++, f--) ray |= 1L << (r * 8 + f);
            RAY_MASKS[sq][4] = ray;

            // NORTH_EAST (ordinal 5)
            ray = 0L;
            for (int r = rank + 1, f = file + 1; r <= 7 && f <= 7; r++, f++) ray |= 1L << (r * 8 + f);
            RAY_MASKS[sq][5] = ray;

            // SOUTH_WEST (ordinal 6)
            ray = 0L;
            for (int r = rank - 1, f = file - 1; r >= 0 && f >= 0; r--, f--) ray |= 1L << (r * 8 + f);
            RAY_MASKS[sq][6] = ray;

            // SOUTH_EAST (ordinal 7)
            ray = 0L;
            for (int r = rank - 1, f = file + 1; r >= 0 && f <= 7; r--, f++) ray |= 1L << (r * 8 + f);
            RAY_MASKS[sq][7] = ray;
        }
    }

    @FunctionalInterface
    private interface MaskComputer {

        long compute(int square);
    }

    @FunctionalInterface
    private interface AttackComputer {

        long compute(int square, long blockers);
    }

    private static void initializeMagicTable(long[] masks, long[][] table, int[] shifts, long[] magics,
            MaskComputer computeMask, AttackComputer computeAttacks) {
        for (int sq = 0; sq < 64; sq++) {
            masks[sq] = computeMask.compute(sq);

            int numBits = shifts[sq];
            table[sq] = new long[1 << numBits];

            long[] blockerConfigs = enumerateBlockerConfigs(masks[sq]);
            for (long blockers : blockerConfigs) {
                int index = (int) ((blockers * magics[sq]) >>> (64 - numBits));
                table[sq][index] = computeAttacks.compute(sq, blockers);
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

    private static long computeBishopMask(int square) {
        long mask = 0L;
        int rank = square / 8;
        int file = square % 8;

        // North-East (exclude edges)
        for (int r = rank + 1, f = file + 1; r < 7 && f < 7; r++, f++) {
            mask |= 1L << (r * 8 + f);
        }
        // North-West (exclude edges)
        for (int r = rank + 1, f = file - 1; r < 7 && f > 0; r++, f--) {
            mask |= 1L << (r * 8 + f);
        }
        // South-East (exclude edges)
        for (int r = rank - 1, f = file + 1; r > 0 && f < 7; r--, f++) {
            mask |= 1L << (r * 8 + f);
        }
        // South-West (exclude edges)
        for (int r = rank - 1, f = file - 1; r > 0 && f > 0; r--, f--) {
            mask |= 1L << (r * 8 + f);
        }

        return mask;
    }

    private static long computeBishopAttacks(int square, long blockers) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        // North-East
        for (int r = rank + 1, f = file + 1; r <= 7 && f <= 7; r++, f++) {
            long sq = 1L << (r * 8 + f);
            attacks |= sq;
            if ((blockers & sq) != 0) {
                break;
            }
        }
        // North-West
        for (int r = rank + 1, f = file - 1; r <= 7 && f >= 0; r++, f--) {
            long sq = 1L << (r * 8 + f);
            attacks |= sq;
            if ((blockers & sq) != 0) {
                break;
            }
        }
        // South-East
        for (int r = rank - 1, f = file + 1; r >= 0 && f <= 7; r--, f++) {
            long sq = 1L << (r * 8 + f);
            attacks |= sq;
            if ((blockers & sq) != 0) {
                break;
            }
        }
        // South-West
        for (int r = rank - 1, f = file - 1; r >= 0 && f >= 0; r--, f--) {
            long sq = 1L << (r * 8 + f);
            attacks |= sq;
            if ((blockers & sq) != 0) {
                break;
            }
        }

        return attacks;
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
