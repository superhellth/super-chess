package com.superhellth.basics;

import java.util.ArrayList;
import java.util.List;

import com.superhellth.utils.BoardUtils;

public class Game {

    private Board board;
    private PseudoLegalMoveGenerator moveGenerator;

    public Game() {
        this.board = new Board(Board.STARTING_FEN);
        this.moveGenerator = new PseudoLegalMoveGenerator(this.board);
        this.moveGenerator.generateAllMoves();
    }

    public void executeMove(Move move) {
        int fromSquare = move.getFromSquare();
        int toSquare = move.getToSquare();

        Color sourceColor = this.board.getSquareColor(fromSquare);
        assert sourceColor == this.board.getActiveColor() : "Executing move of inactive color!";
        PieceType sourceType = this.board.getSquarePieceType(fromSquare);
        assert (sourceColor != Color.EMPTY) && (sourceType != PieceType.EMPTY) : "Empty piece / color making a move!";

        this.board.removePiece(fromSquare);
        this.board.removePiece(toSquare);
        this.board.placePiece(sourceColor, sourceType, toSquare);

        this.board.setActiveColor(BoardUtils.getOppositeColor(sourceColor));
        this.moveGenerator.generateAllMoves();
    }

    public List<Move> getPseudoLegalMovesFromSquare(int squareIndex) {
        Color squareColor = this.board.getSquareColor(squareIndex);
        if (squareColor == this.board.getActiveColor()) {
            return this.moveGenerator.getAllMovesBySquareList(squareIndex);
        } else {
            return new ArrayList<>();
        }
    }

    public Board getBoard() {
        return this.board;
    }

    public PseudoLegalMoveGenerator getMoveGenerator() {
        return this.moveGenerator;
    }
}
