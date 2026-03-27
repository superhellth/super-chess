package com.superhellth.ui;

import com.superhellth.basics.Board;
import com.superhellth.basics.Color;
import com.superhellth.basics.Direction;
import com.superhellth.basics.PieceType;
import com.superhellth.basics.PseudoLegalMoveGenerator;
import com.superhellth.utils.BitboardUtils;
import com.superhellth.utils.BoardUtils;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class BoardGrid extends GridPane {

    private final Board board;
    private final PseudoLegalMoveGenerator moveGenerator;
    private final BoardSquare[] squares = new BoardSquare[64];
    private BoardSquare selectedSquare = null;
    private long highlightedBitboard = 0L;

    public BoardGrid(Board board, PseudoLegalMoveGenerator moveGenerator) {
        super();
        this.board = board;
        this.moveGenerator = moveGenerator;

        // Initialize squares
        for (int squareIndex = 0; squareIndex < 64; squareIndex++) {
            this.squares[squareIndex] = new BoardSquare(squareIndex, this::handleSquareClick);
            int[] rankAndFile = BoardUtils.getRankAndFileFromSquareIndex(squareIndex);
            this.add(this.squares[squareIndex], rankAndFile[0], rankAndFile[1]);
        }
        this.setupPieces();

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

    public void shiftHighlightedBitboard(Direction direction) {
        this.highlightedBitboard = BitboardUtils.shift(this.highlightedBitboard, direction);
        this.visualizeBitboard(this.highlightedBitboard);
    }

    public void visualizeBitboard(long bitboard) {
        this.highlightedBitboard = bitboard;
        for (int i = 0; i < 64; i++) {
            this.squares[i].resetHighlight();
        }
        for (int i : BitboardUtils.getPopulatedIndices(bitboard)) {
            this.squares[i].highlight("yellow");
        }
    }

    private void setupPieces() {
        for (Color color : new Color[]{Color.WHITE, Color.BLACK}) {
            for (PieceType pieceType : PieceType.values()) {
                if (pieceType == PieceType.EMPTY) {
                    continue;
                }
                long bitboard = this.board.getPieceBitboard(color, pieceType);
                if (bitboard == 0) {
                    continue;
                }
                for (int i : BitboardUtils.getPopulatedIndices(bitboard)) {
                    this.squares[i].setPiece(pieceType, color);
                }
            }
        }
    }

    private void handleSquareClick(BoardSquare square) {
        if (this.selectedSquare != null) {
            this.selectedSquare.resetHighlight();
        }

        if (this.selectedSquare == square) {
            this.selectedSquare = null;
        } else {
            this.selectedSquare = square;
            this.visualizeBitboard(this.moveGenerator.getAllMovesBySquareBitboard(square.getSquareIndex()));
            square.highlight("red");
        }
    }

}
