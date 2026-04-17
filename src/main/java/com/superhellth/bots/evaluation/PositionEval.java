package com.superhellth.bots.evaluation;

import java.util.List;
import java.util.Map;

import com.superhellth.basics.Board;
import com.superhellth.basics.Color;
import com.superhellth.basics.Move;
import com.superhellth.basics.PieceType;

public class PositionEval {

    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final Map<PieceType, Integer> PIECE_VALUES = Map.of(
            PieceType.PAWN, PAWN_VALUE,
            PieceType.KNIGHT, KNIGHT_VALUE,
            PieceType.BISHOP, BISHOP_VALUE,
            PieceType.ROOK, ROOK_VALUE,
            PieceType.QUEEN, QUEEN_VALUE,
            PieceType.KING, 20000
    );

    public static double evaluatePosition(Board board) {
        double whitePieceValue = 0.0;
        double blackPieceValue = 0.0;

        for (Color color : Color.values()) {
            if (color == Color.EMPTY) {
                continue;
            }
            for (PieceType pieceType : PieceType.values()) {
                if (pieceType == PieceType.EMPTY) {
                    continue;
                }
                long pieceBitboard = board.getPieceBitboard(color, pieceType);
                int pieceCount = Long.bitCount(pieceBitboard);
                double pieceValue = pieceCount * PIECE_VALUES.get(pieceType);
                if (color == Color.WHITE) {
                    whitePieceValue += pieceValue;
                } else {
                    blackPieceValue += pieceValue;
                }
            }
        }
        if (board.getActiveColor() == Color.WHITE) {
            return whitePieceValue - blackPieceValue;
        }
        return blackPieceValue - whitePieceValue;
    }

    public static void orderMoves(List<Move> moves, Board board, long enemyAttacksBitboard) {
        moves.sort((a, b) -> Double.compare(scoreMove(b, board, enemyAttacksBitboard), scoreMove(a, board, enemyAttacksBitboard)));
    }

    private static double scoreMove(Move move, Board board, long enemyAttacksBitboard) {
        double moveValue = 0.0;
        int sourceSquare = move.getFromSquare();
        int targetSquare = move.getToSquare();
        PieceType sourcePieceType = board.getSquarePieceType(sourceSquare);
        PieceType targetPieceType = board.getSquarePieceType(targetSquare);

        if (targetPieceType != PieceType.EMPTY) {
            moveValue += 10 * PIECE_VALUES.get(targetPieceType) - PIECE_VALUES.get(sourcePieceType);
        }

        if (move.getPromotionPieceType() != PieceType.EMPTY) {
            moveValue += PIECE_VALUES.get(move.getPromotionPieceType());
        }

        if ((enemyAttacksBitboard & (1L << targetSquare)) != 0) {
            moveValue -= PIECE_VALUES.get(sourcePieceType);
        }

        return moveValue;
    }

}
