package com.superhellth.basics;

public class Game {

    private Board board;
    private PseudoLegalMoveGenerator moveGenerator;
    private PseudoLegalMoveProvider moveProvider;

    public Game() {
        this.board = new Board();
        this.moveGenerator = new PseudoLegalMoveGenerator(this.board);
        this.moveProvider = new PseudoLegalMoveProvider(this.moveGenerator);
    }

    public Board getBoard() {
        return this.board;
    }

    public PseudoLegalMoveGenerator getMoveGenerator() {
        return this.moveGenerator;
    }

    public PseudoLegalMoveProvider getMoveProvider() {
        return this.moveProvider;
    }
}
