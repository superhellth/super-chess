package com.superhellth.bots;

import java.util.ArrayList;
import java.util.List;

import com.superhellth.basics.Color;
import com.superhellth.basics.Move;
import com.superhellth.basics.MoveUndo;
import com.superhellth.bots.evaluation.PositionEval;
import com.superhellth.utils.BoardUtils;

public class EvalBot1 extends Bot {

    private final int searchDepth;

    public EvalBot1(int searchDepth) {
        super();
        this.searchDepth = searchDepth;
    }

    @Override
    public Move selectMoveImpl() {
        List<Move> legalMoves = new ArrayList<>(this.moveGenerator.getLegalMoves());
        Color activeColor = this.game.getBoard().getActiveColor();
        long enemyAttacks = this.moveGenerator.getAttackBitboardByColor(BoardUtils.getOppositeColor(activeColor));
        PositionEval.orderMoves(legalMoves, this.game.getBoard(), enemyAttacks);

        Move bestMove = null;
        double bestEval = Double.NEGATIVE_INFINITY;

        for (Move move : legalMoves) {
            MoveUndo undo = this.game.makeMove(move);
            double eval = -this.search(this.searchDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            this.game.undoMove(move, undo);
            if (eval >= bestEval) {
                bestEval = eval;
                bestMove = move;
            }
        }
        assert bestMove != null : "There should always be at least one legal move when selectMoveImpl is called.";

        return bestMove;
    }

    private double search(int depth, double alpha, double beta) {
        List<Move> legalMoves = this.moveGenerator.getLegalMoves();

        // check / stale mate
        if (legalMoves.isEmpty()) {
            if (this.game.getGameState().isDraw()) {
                return 0.0;
            } else {
                return Double.NEGATIVE_INFINITY;
            }
        }

        // default case
        if (depth == 0) {
            return PositionEval.evaluatePosition(this.game.getBoard());
        }

        // order moves for better alpha-beta pruning
        legalMoves = new ArrayList<>(legalMoves);
        Color activeColor = this.game.getBoard().getActiveColor();
        long enemyAttacks = this.moveGenerator.getAttackBitboardByColor(BoardUtils.getOppositeColor(activeColor));
        PositionEval.orderMoves(legalMoves, this.game.getBoard(), enemyAttacks);

        // deeper search
        for (Move move : legalMoves) {
            MoveUndo undo = this.game.makeMove(move);
            double eval = -search(depth - 1, -beta, -alpha);
            this.game.undoMove(move, undo);
            if (eval >= beta) {
                return beta;
            }
            alpha = Math.max(alpha, eval);
        }
        return alpha;
    }

    @Override
    public String getName() {
        return "Eval Bot 1 (Material Only)";
    }

    @Override
    public String getDescription() {
        return "A bot that evaluates positions based on material balance and selects the move with the highest evaluation.";
    }
}
