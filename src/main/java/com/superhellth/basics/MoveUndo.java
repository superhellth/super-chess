package com.superhellth.basics;

/**
 * Stores the minimal state needed to undo a move.
 */
public class MoveUndo {

    final PieceType capturedType;
    final Color capturedColor;
    final boolean[] castlingRights;
    final int enPassantSquare;
    final int halfmoveClock;

    public MoveUndo(PieceType capturedType, Color capturedColor, boolean[] castlingRights, int enPassantSquare, int halfmoveClock) {
        this.capturedType = capturedType;
        this.capturedColor = capturedColor;
        this.castlingRights = castlingRights;
        this.enPassantSquare = enPassantSquare;
        this.halfmoveClock = halfmoveClock;
    }
}
