package com.superhellth.basics;

public class Game {

    private Board board;
    private PseudoLegalMoveGenerator moveGenerator;

    public Game() {
        this.board = new Board();
        this.moveGenerator = new PseudoLegalMoveGenerator(this.board);
        this.moveGenerator.generateAllMoves();
    }

    public void executeMove(Move move) {
        int fromSquare = move.getFromSquare();
        int toSquare = move.getToSquare();

        Color sourceColor = this.board.getSquareColor(fromSquare);
        PieceType sourceType = this.board.getSquarePieceType(fromSquare);

        this.board.removePiece(fromSquare);
        this.board.removePiece(toSquare);
        this.board.placePiece(sourceColor, sourceType, toSquare);

        this.moveGenerator.generateAllMoves();
    }

    public Board getBoard() {
        return this.board;
    }

    public PseudoLegalMoveGenerator getMoveGenerator() {
        return this.moveGenerator;
    }
}
