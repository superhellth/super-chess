package com.superhellth.basics;

public enum GameState {
    ONGOING,
    WHITE_WINS_BY_CHECKMATE,
    BLACK_WINS_BY_CHECKMATE,
    DRAW_BY_STALEMATE,
    DRAW_BY_REPETITION,
    DRAW_BY_FIFTY_MOVE_RULE,
    DRAW_BY_INSUFFICIENT_MATERIAL,
    DRAW_BY_MOVE_LIMIT;

    public boolean isOngoing() {
        return this == ONGOING;
    }

    public boolean isDraw() {
        return this == DRAW_BY_STALEMATE || this == DRAW_BY_REPETITION
                || this == DRAW_BY_FIFTY_MOVE_RULE || this == DRAW_BY_INSUFFICIENT_MATERIAL
                || this == DRAW_BY_MOVE_LIMIT;
    }

    public boolean isWhiteWin() {
        return this == WHITE_WINS_BY_CHECKMATE;
    }

    public boolean isBlackWin() {
        return this == BLACK_WINS_BY_CHECKMATE;
    }

    public Color getWinColor() {
        if (this == WHITE_WINS_BY_CHECKMATE) {
            return Color.WHITE;
        } else if (this == BLACK_WINS_BY_CHECKMATE) {
            return Color.BLACK;
        }
        return Color.EMPTY;
    }
}
