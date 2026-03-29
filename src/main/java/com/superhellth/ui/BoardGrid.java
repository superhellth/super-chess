package com.superhellth.ui;

import com.superhellth.basics.Board;
import com.superhellth.basics.Color;
import com.superhellth.basics.Game;
import com.superhellth.basics.Move;
import com.superhellth.basics.PieceType;
import com.superhellth.utils.BoardUtils;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class BoardGrid extends GridPane {

    private final Game game;
    private final Board board;
    private final BoardSquare[] squares = new BoardSquare[64];
    private BoardSquare selectedSquare = null;

    public BoardGrid(Game game) {
        super();
        this.game = game;
        this.board = game.getBoard();

        // Initialize squares
        for (int squareIndex = 0; squareIndex < 64; squareIndex++) {
            this.squares[squareIndex] = new BoardSquare(squareIndex, this::handleSquareClick);
            int[] rankAndFile = BoardUtils.getRankAndFileFromSquareIndex(squareIndex);
            this.add(this.squares[squareIndex], rankAndFile[1], 7 - rankAndFile[0]);
        }
        this.loadBoard();

        // Styling
        for (int i = 0; i < 8; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(12.5);
            col.setFillWidth(true);
            getColumnConstraints().add(col);

            RowConstraints row = new RowConstraints();
            row.setPercentHeight(12.5);
            row.setFillHeight(true);
            getRowConstraints().add(row);
        }
    }

    private void loadBoard() {
        for (int i = 0; i < 64; i++) {
            PieceType pieceType = this.board.getSquarePieceType(i);
            Color pieceColor = this.board.getSquareColor(i);
            this.squares[i].setPiece(pieceType, pieceColor);
        }
    }

    private void handleSquareClick(BoardSquare square) {
        int squareIndex = square.getSquareIndex();
        Move squareTargetMove = square.getTargetMove();

        if (this.selectedSquare != null) {
            this.selectedSquare.resetHighlight();
            this.resetAllTargetMoves();
        }

        if (this.selectedSquare == square) {
            this.selectedSquare = null;
        } else if (squareTargetMove != null) {
            this.game.executeMove(squareTargetMove);
            this.loadBoard();
        } else  {
            this.selectedSquare = square;
            for (Move move : this.game.getPseudoLegalMovesFromSquare(squareIndex)) {
                int targetSquareIndex = move.getToSquare();
                this.squares[targetSquareIndex].setTargetMove(move);
            }
            square.select();
        }
    }

    private void resetAllTargetMoves() {
        for (int i = 0; i < 64; i++) {
            this.squares[i].setTargetMove(null);
        }
    }

}
