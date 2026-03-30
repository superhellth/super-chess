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
    private Move pendingPromotionMove = null;
    private int[] promotionOptionSquares = null;

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
            this.squares[i].resetHighlight();
            this.squares[i].setTargetMove(null, false);
        }
    }

    private void handleSquareClick(BoardSquare square) {
        // Handle click during promotion selection
        if (this.pendingPromotionMove != null) {
            PieceType chosenPiece = square.getPromotionOption();
            clearPromotionOptions();
            if (chosenPiece != null) {
                this.pendingPromotionMove.setPromotionPieceType(chosenPiece);
                this.game.executeMove(this.pendingPromotionMove);
            }
            this.pendingPromotionMove = null;
            this.selectedSquare = null;
            this.loadBoard();
            return;
        }

        int squareIndex = square.getSquareIndex();
        Move squareTargetMove = square.getTargetMove();

        if (this.selectedSquare != null) {
            this.selectedSquare.resetHighlight();
            this.resetAllTargetMoves();
        }

        if (this.selectedSquare == square) {
            this.selectedSquare = null;
        } else if (squareTargetMove != null) {
            if (square.isTargetMovePromotion()) {
                showPromotionOptions(squareTargetMove);
            } else {
                this.game.executeMove(squareTargetMove);
                this.selectedSquare = null;
                this.loadBoard();
            }
        } else {
            this.selectedSquare = square;
            for (Move move : this.game.getPseudoLegalMovesFromSquare(squareIndex)) {
                int targetSquareIndex = move.getToSquare();
                boolean isCapture = this.board.getSquareColor(targetSquareIndex) != Color.EMPTY;
                this.squares[targetSquareIndex].setTargetMove(move, isCapture);
                this.squares[targetSquareIndex].setTargetMoveIsPromotion(move.getPromotionPieceType() != PieceType.EMPTY);
            }
            square.select();
        }
    }

    private void showPromotionOptions(Move move) {
        this.pendingPromotionMove = move;
        if (this.selectedSquare != null) {
            this.selectedSquare.resetHighlight();
        }
        this.resetAllTargetMoves();

        int targetSquare = move.getToSquare();
        int file = targetSquare % 8;
        int rank = targetSquare / 8;
        Color pieceColor = this.board.getSquareColor(move.getFromSquare());

        // White promotes on rank 7 -> extend downward; black promotes on rank 0 -> extend upward
        int rankStep = (rank == 7) ? -1 : 1;

        PieceType[] options = { PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT };
        this.promotionOptionSquares = new int[4];

        for (int i = 0; i < 4; i++) {
            int optionRank = rank + i * rankStep;
            int squareIndex = optionRank * 8 + file;
            this.promotionOptionSquares[i] = squareIndex;
            this.squares[squareIndex].showPromotionOption(options[i], pieceColor);
        }
    }

    private void clearPromotionOptions() {
        if (this.promotionOptionSquares != null) {
            for (int squareIndex : this.promotionOptionSquares) {
                this.squares[squareIndex].clearPromotionOption();
            }
            this.promotionOptionSquares = null;
        }
    }

    private void resetAllTargetMoves() {
        for (int i = 0; i < 64; i++) {
            this.squares[i].setTargetMove(null, false);
        }
    }

}
