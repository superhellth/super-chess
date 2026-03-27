package com.superhellth.basics;

import java.util.ArrayList;
import java.util.List;

import com.superhellth.utils.BitboardUtils;
import com.superhellth.utils.BoardUtils;

public class PseudoLegalMoveGenerator {

    public static final long[] KNIGHT_ATTACKS = new long[64];

    private final Board board;
    private List<Move> allMoves;
    private List<Move>[] movesByColor;
    private final long[] pawnPushTargets = new long[2]; // 0 white, 1 black
    private final long[] pawnAttackTargets = new long[2]; // 0 white, 1 black

    public PseudoLegalMoveGenerator(Board board) {
        this.board = board;
        PseudoLegalMoveGenerator.initializeKnightAttacks();
    }

    public static void initializeKnightAttacks() {
        for (int square = 0; square < 64; square++) {
            long knightBitboard = 1L << square;
            long attacks = 0L;

            attacks |= (knightBitboard << 17) & 0xFEFEFEFEFEFEFEFEL;
            attacks |= (knightBitboard << 15) & 0x7F7F7F7F7F7F7F7FL;
            // ...

            KNIGHT_ATTACKS[square] = attacks;
        }
    }

    public List<Move> generateAllMoves() {
        this.allMoves = new ArrayList<>();
        this.allMoves.addAll(this.generateAllMovesByColor(Color.WHITE));
        this.allMoves.addAll(this.generateAllMovesByColor(Color.BLACK));
        return this.allMoves;
    }

    public List<Move> generateAllMovesByColor(Color color) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when getting legal moves by color";
        this.movesByColor[color.ordinal()] = new ArrayList<>();
        this.movesByColor[color.ordinal()].addAll(this.generatePawnMoves(color));
        return this.movesByColor[color.ordinal()];
    }

    // Getters for moves by square
    public List<Move> getAllMovesBySquareList(int squareIndex) {
        return this.allMoves.stream()
                .filter(move -> move.getFromSquare() == squareIndex)
                .toList();
    }

    public long getAllMovesBySquareBitboard(int squareIndex) {
        return this.allMoves.stream()
                .filter(move -> move.getFromSquare() == squareIndex)
                .reduce(0L, (bitboard, move) -> bitboard | (1L << move.getToSquare()), (a, b) -> a | b);
    }

    // Bitboard getters for UI visualization
    public long getPawnPushTargets(Color color) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when getting pawn push targets";
        return this.pawnPushTargets[color.ordinal()];
    }

    public long getPawnAttackTargets(Color color) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when getting pawn attack targets";
        return this.pawnAttackTargets[color.ordinal()];
    }

    private List<Move> generatePawnMoves(Color color) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when generating pawn push targets";

        // Prepare bitboards
        long pawnBitboard = board.getPieceBitboard(color, PieceType.PAWN);
        long emptyBitboard = board.getOccupancyBitboard(Color.EMPTY);
        long oppositeColorBitboard = board.getOccupancyBitboard(BoardUtils.getOppositeColor(color));

        // Generate targets
        long singlePushTargets = generateSinglePushTargets(color, pawnBitboard, emptyBitboard);
        long doublePushTargets = generateDoublePushTargets(color, singlePushTargets, emptyBitboard);
        this.pawnPushTargets[color.ordinal()] = singlePushTargets | doublePushTargets;
        long eastAttacks = generatePawnAttacks(color, Direction.EAST, pawnBitboard, oppositeColorBitboard);
        long westAttacks = generatePawnAttacks(color, Direction.WEST, pawnBitboard, oppositeColorBitboard);
        this.pawnAttackTargets[color.ordinal()] = eastAttacks | westAttacks;

        // Extract moves from targets
        List<Move> moves = new ArrayList<>();
        moves.addAll(this.generateMovesFromBitboard(singlePushTargets, color == Color.WHITE ? 8 : -8));
        moves.addAll(this.generateMovesFromBitboard(doublePushTargets, color == Color.WHITE ? 16 : -16));
        moves.addAll(this.generateMovesFromBitboard(eastAttacks, color == Color.WHITE ? 9 : -7));
        moves.addAll(this.generateMovesFromBitboard(westAttacks, color == Color.WHITE ? 7 : -9));

        return moves;
    }

    // private void generateKnightMoves(long knightBitboard, long occupancyBitboard) {
    //     long knightsToProcess = knightBitboard;

    //     while (knightsToProcess != 0L) {
    //         int fromSquare = Long.numberOfTrailingZeros(knightsToProcess);
    //         long validMoves = KNIGHT_ATTACKS[fromSquare] & ~occupancyBitboard;

    //         while (validMoves != 0L) {
    //             int toSquare = Long.numberOfTrailingZeros(validMoves);
    //             moveList.add(new Move(fromSquare, toSquare));

    //             // Clear the bit to process the next destination
    //             validMoves &= validMoves - 1L;
    //         }

    //         // 4. Clear the processed knight to find the next one
    //         knightsToProcess &= knightsToProcess - 1L;
    //     }
    // }

    private long generatePawnAttacks(Color color, Direction direction, long pawnBitboard, long oppositeColorBitboard) {
        long attacks;
        if (color == Color.WHITE) {
            attacks = BitboardUtils.shift(pawnBitboard, direction == Direction.EAST ? Direction.NORTH_EAST : Direction.NORTH_WEST);
        } else {
            attacks = BitboardUtils.shift(pawnBitboard, direction == Direction.EAST ? Direction.SOUTH_EAST : Direction.SOUTH_WEST);
        }
        return attacks & oppositeColorBitboard;
    }

    private long generateSinglePushTargets(Color color, long pawnBitboard, long emptyBitboard) {
        if (color == Color.WHITE) {
            return BitboardUtils.shift(pawnBitboard, Direction.NORTH) & emptyBitboard;
        }
        return BitboardUtils.shift(pawnBitboard, Direction.SOUTH) & emptyBitboard;
    }

    private long generateDoublePushTargets(Color color, long singlePushTargets, long emptyBitboard) {
        if (color == Color.WHITE) {
            long rank4 = 0x00000000FF000000L;
            return BitboardUtils.shift(singlePushTargets, Direction.NORTH) & rank4 & emptyBitboard;
        }
        long rank5 = 0x000000FF00000000L;
        return BitboardUtils.shift(singlePushTargets, Direction.SOUTH) & rank5 & emptyBitboard;
    }

    private List<Move> generateMovesFromBitboard(long bitboard, int offset) {
        List<Move> moves = new ArrayList<>();
        while (bitboard != 0) {
            int toSquare = Long.numberOfTrailingZeros(bitboard);
            int fromSquare = toSquare - offset;
            moves.add(new Move(fromSquare, toSquare));
            bitboard &= bitboard - 1;
        }
        return moves;
    }
}
