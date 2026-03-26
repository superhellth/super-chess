package com.superhellth.basics;

import com.superhellth.utils.BitboardUtils;

public class MoveGenerator {

    private Board board;

    public MoveGenerator(Board board) {
        this.board = board;
    }

    public long getPawnPushTargets(Color color) {
        long singlePushTargets = generateSinglePushTargets(color);
        long doublePushTargets = generateDoublePushTargets(color, singlePushTargets);
        return singlePushTargets | doublePushTargets;
    }

    private long generateSinglePushTargets(Color color) {
        if (color == Color.WHITE) {
            return BitboardUtils.shiftNorth(board.getBitboard(Color.WHITE, PieceType.PAWN), 1) & board.getBitboard(Color.WHITE, PieceType.EMPTY);
        }
        return BitboardUtils.shiftSouth(board.getBitboard(Color.BLACK, PieceType.PAWN), 1) & board.getBitboard(Color.BLACK, PieceType.EMPTY);
    }

    private long generateDoublePushTargets(Color color, long singlePushTargets) {
        if (color == Color.WHITE) {
            long rank4 = 0x00000000FF000000L;
            return BitboardUtils.shiftNorth(singlePushTargets, 1) & rank4 & board.getBitboard(Color.WHITE, PieceType.EMPTY);
        }
        long rank5 = 0x000000FF00000000L;
        return BitboardUtils.shiftSouth(singlePushTargets, 1) & rank5 & board.getBitboard(Color.BLACK, PieceType.EMPTY);
    }
}
