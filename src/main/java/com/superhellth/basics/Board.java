package com.superhellth.basics;

public class Board {

    private int[] colors;
    private int[] pieces;
    private long[][] bitboards;

    public Board() {
        colors = new int[64];
        pieces = new int[64];
        bitboards = new long[2][6];

        this.setupPieces();
    }

    public void setupPieces() {
        for (int i = 0; i < 8; i++) {
            colors[i] = Color.WHITE.ordinal();
            pieces[i] = PieceType.PAWN.ordinal();
        }
        bitboards[Color.WHITE.ordinal()][PieceType.PAWN.ordinal()] = 0x000000000000FF00L;
        bitboards[Color.WHITE.ordinal()][PieceType.KNIGHT.ordinal()] = 0x0000000000000042L;
        bitboards[Color.WHITE.ordinal()][PieceType.BISHOP.ordinal()] = 0x0000000000000024L;
        bitboards[Color.WHITE.ordinal()][PieceType.ROOK.ordinal()] = 0x0000000000000081L;
        bitboards[Color.WHITE.ordinal()][PieceType.QUEEN.ordinal()] = 0x0000000000000008L;
        bitboards[Color.WHITE.ordinal()][PieceType.KING.ordinal()] = 0x0000000000000010L;
        bitboards[Color.BLACK.ordinal()][PieceType.PAWN.ordinal()] = 0x00FF000000000000L;
        bitboards[Color.BLACK.ordinal()][PieceType.KNIGHT.ordinal()] = 0x4200000000000000L;
        bitboards[Color.BLACK.ordinal()][PieceType.BISHOP.ordinal()] = 0x2400000000000000L;
        bitboards[Color.BLACK.ordinal()][PieceType.ROOK.ordinal()] = 0x8100000000000000L;
        bitboards[Color.BLACK.ordinal()][PieceType.QUEEN.ordinal()] = 0x0800000000000000L;
        bitboards[Color.BLACK.ordinal()][PieceType.KING.ordinal()] = 0x1000000000000000L;
    }

    public long getBitboard(Color color, PieceType pieceType) {
        return bitboards[color.ordinal()][pieceType.ordinal()];
    }

}
