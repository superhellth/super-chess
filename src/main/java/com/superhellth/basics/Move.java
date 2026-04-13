package com.superhellth.basics;

public class Move {
    private final int fromSquare;
    private final int toSquare;
    private PieceType promotionPieceType;

    public Move(int fromSquare, int toSquare) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.promotionPieceType = PieceType.EMPTY;
    }

    public void setPromotionPieceType(PieceType promotionPieceType) {
        this.promotionPieceType = promotionPieceType;
    }

    public PieceType getPromotionPieceType() {
        return this.promotionPieceType;
    }

    public int getFromSquare() {
        return this.fromSquare;
    }

    public int getToSquare() {
        return this.toSquare;
    }
}