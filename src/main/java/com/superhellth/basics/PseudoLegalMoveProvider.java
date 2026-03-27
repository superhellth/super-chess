package com.superhellth.basics;

import java.util.ArrayList;
import java.util.List;

public class PseudoLegalMoveProvider {

    private PseudoLegalMoveGenerator moveGenerator;
    private long[][] pawnPushTargets = new long[2][2]; // [color][0: single push, 1: double push]
    private long[][] pawnAttackTargets = new long[2][2]; // [color][0: east attacks, 1: west attacks]
    private List<Move> allMoves = new ArrayList<>();

    public PseudoLegalMoveProvider(PseudoLegalMoveGenerator moveGenerator) {
        this.moveGenerator = moveGenerator;
    }

    public List<Move> getLegalMovesList(int squareIndex) {
        this.generateAllMoves();
        return this.allMoves.stream()
                .filter(move -> move.getFromSquare() == squareIndex)
                .toList();
    }

    public long getLegalMovesBitboard(int squareIndex) {
        long bitboard = 0L;
        for (Move move : this.getLegalMovesList(squareIndex)) {
            bitboard |= 1L << move.getToSquare();
        }
        return bitboard;
    }

    private void generateAllMoves() {
        allMoves.clear();
        this.generateAllBitboards();
        for (Color color : Color.values()) {
            if (color == Color.EMPTY) {
                continue;
            }
            generateMovesFromBitboard(this.pawnPushTargets[color.ordinal()][0], color == Color.WHITE ? 8 : -8);
            generateMovesFromBitboard(this.pawnPushTargets[color.ordinal()][1], color == Color.WHITE ? 16 : -16);
            generateMovesFromBitboard(this.pawnAttackTargets[color.ordinal()][0], color == Color.WHITE ? 9 : -7);
            generateMovesFromBitboard(this.pawnAttackTargets[color.ordinal()][1], color == Color.WHITE ? 7 : -9);
        }
    }

    private void generateMovesFromBitboard(long bitboard, int offset) {
        while (bitboard != 0) {
            int toSquare = Long.numberOfTrailingZeros(bitboard);
            int fromSquare = toSquare - offset;
            this.allMoves.add(new Move(fromSquare, toSquare));
            bitboard &= bitboard - 1;
        }
    }

    private void generateAllBitboards() {
        for (Color color : Color.values()) {
            if (color == Color.EMPTY) {
                continue;
            }

            // Pawn moves
            this.pawnPushTargets[color.ordinal()] = this.moveGenerator.getPawnPushTargets(color);
            this.pawnAttackTargets[color.ordinal()][0] = this.moveGenerator.getPawnAttackTargets(color, Direction.EAST);
            this.pawnAttackTargets[color.ordinal()][1] = this.moveGenerator.getPawnAttackTargets(color, Direction.WEST);
        }
    }

}
