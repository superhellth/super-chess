package com.superhellth.basics;

import com.superhellth.utils.BitboardUtils;
import com.superhellth.utils.BoardUtils;

public class PseudoLegalMoveGenerator {

    private Board board;

    public PseudoLegalMoveGenerator(Board board) {
        this.board = board;
    }

    public long[] getPawnPushTargets(Color color) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when generating pawn push targets";
        long pawnBitboard = board.getPieceBitboard(color, PieceType.PAWN);
        long emptyBitboard = board.getOccupancyBitboard(Color.EMPTY);
        long singlePushTargets = generateSinglePushTargets(color, pawnBitboard, emptyBitboard);
        long doublePushTargets = generateDoublePushTargets(color, singlePushTargets, emptyBitboard);
        return new long[]{singlePushTargets, doublePushTargets};
    }

    public long getPawnAttackTargets(Color color, Direction direction) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when generating pawn attacks";
        long pawnBitboard = board.getPieceBitboard(color, PieceType.PAWN);
        long oppositeColorBitboard = board.getOccupancyBitboard(BoardUtils.getOppositeColor(color));
        return generatePawnAttacks(color, direction, pawnBitboard, oppositeColorBitboard);
    }

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
}
