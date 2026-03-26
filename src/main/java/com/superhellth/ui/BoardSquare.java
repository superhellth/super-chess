package com.superhellth.ui;

import com.superhellth.basics.Color;
import com.superhellth.basics.PieceType;
import com.superhellth.utils.BoardUtils;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BoardSquare extends Button {

    private int file;
    private int rank;
    private PieceType pieceType;
    private Color pieceColor;

    public BoardSquare(int file, int rank) {
        super();
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.setStyle("-fx-background-color: " + (BoardUtils.isLightSquare(file, rank) ? "white" : "beige"));
        // this.setStyle("-fx-font-color: red");
    }

    public void setPiece(PieceType pieceType, Color pieceColor) {
        this.pieceType = pieceType;
        this.pieceColor = pieceColor;
        if (pieceType != null && pieceType != PieceType.EMPTY) {
            String path = "/images/" + pieceColor.name().toLowerCase()
                    + "-" + pieceType.name().toLowerCase() + ".png";
            Image image = new Image(getClass().getResourceAsStream(path));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.fitWidthProperty().bind(this.widthProperty().multiply(0.8));
            imageView.fitHeightProperty().bind(this.heightProperty().multiply(0.8));
            this.setGraphic(imageView);
            this.setText("");
        } else {
            this.setGraphic(null);
            this.setText("");
        }
    }

}
