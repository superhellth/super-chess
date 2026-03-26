package com.superhellth.ui;

import com.superhellth.basics.Color;
import com.superhellth.basics.PieceType;
import com.superhellth.utils.BoardUtils;

import javafx.scene.control.Button;

public class BoardSquare extends Button {

    private int file;
    private int rank;
    private PieceType pieceType;
    private Color pieceColor;

    public BoardSquare(int file, int rank) {
        super();
        this.setPrefSize(40, 40);
        this.setStyle("-fx-background-color: " + (BoardUtils.isLightSquare(file, rank) ? "white" : "black"));
        // this.setStyle("-fx-font-color: red");
    }

    public void setPieceColor(Color pieceColor) {
        this.pieceColor = pieceColor;
        if (pieceColor == Color.WHITE) {
            this.setStyle(this.getStyle() + "; -fx-text-fill: white;");
        } else if (pieceColor == Color.BLACK) {
            this.setStyle(this.getStyle() + "; -fx-text-fill: black;");
        }
    }

    public void setPieceType(PieceType pieceType) {
        this.pieceType = pieceType;
        if (pieceType != null && pieceType != PieceType.EMPTY) {
            this.setText(pieceType.toString().substring(0, 1));
        } else {
            this.setText("");
        }
    }

}
