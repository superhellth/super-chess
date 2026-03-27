package com.superhellth.basics;

public class Game {

    private Board board;
    private PseudoLegalMoveGenerator moveGenerator;

    public Game() {
        this.board = new Board();
        this.moveGenerator = new PseudoLegalMoveGenerator(this.board);
        this.moveGenerator.generateAllMoves();
    }

    public Board getBoard() {
        return this.board;
    }

    public PseudoLegalMoveGenerator getMoveGenerator() {
        return this.moveGenerator;
    }
}
