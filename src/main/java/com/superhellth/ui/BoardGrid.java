package com.superhellth.ui;

import com.superhellth.basics.Board;
import com.superhellth.basics.Color;
import com.superhellth.basics.PieceType;
import com.superhellth.utils.BoardUtils;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class BoardGrid extends GridPane {

    private Board board;
    private BoardSquare[][] squares = new BoardSquare[8][8];

    public BoardGrid(Board board) {
        super();
        this.board = board;
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                this.squares[file][rank] = new BoardSquare(file, rank);
                this.add(this.squares[file][rank], file, rank);
            }
        }
        this.setupPieces();

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

    private void setupPieces() {
        for (Color color : Color.values()) {
            for (PieceType pieceType : PieceType.values()) {
                if (pieceType == PieceType.EMPTY) {
                    continue;
                }
                long bitboard = this.board.getBitboard(color, pieceType);
                if (bitboard == 0) {
                    continue;
                }
                for (int i : BoardUtils.getPopulatedIndices(bitboard)) {
                    int[] rankAndFile = BoardUtils.getRankAndFileFromSquareIndex(i);
                    int file = rankAndFile[0];
                    int rank = rankAndFile[1];
                    this.squares[file][rank].setPiece(pieceType, color);
                }
            }
        }
    }

}
