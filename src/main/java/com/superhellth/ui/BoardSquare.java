package com.superhellth.ui;

import com.superhellth.basics.Color;
import com.superhellth.basics.PieceType;
import com.superhellth.utils.BoardUtils;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class BoardSquare extends Button {

    private int file;
    private int rank;
    private PieceType pieceType;
    private Color pieceColor;

    public BoardSquare(int file, int rank) {
        super();
        this.file = file;
        this.rank = rank;
        this.pieceType = PieceType.EMPTY;
        this.pieceColor = null;

        // Style
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.resetHighlight();

        // Logic
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            
        });
    }

    public void highlight() {
        this.setStyle("-fx-background-color: red");
    }

    public void resetHighlight() {
        this.setStyle("-fx-background-color: " + (BoardUtils.isLightSquare(this.file, this.rank) ? "white" : "beige"));
    }

    public void setPiece(PieceType pieceType, Color pieceColor) {
        this.pieceType = pieceType;
        this.pieceColor = pieceColor;
        if (pieceType != null && pieceType != PieceType.EMPTY) {
            String path = "/images/" + pieceColor.name().toLowerCase()
                    + "-" + pieceType.name().toLowerCase() + ".png";
            Image image = new Image(getClass().getResourceAsStream(path), 120, 120, true, true);
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
