package com.superhellth.ui;

import java.util.function.Consumer;

import com.superhellth.basics.Color;
import com.superhellth.basics.Move;
import com.superhellth.basics.PieceType;
import com.superhellth.utils.BoardUtils;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class BoardSquare extends Button {

    private final int squareIndex;
    private PieceType pieceType;
    private Color pieceColor;
    private Move targetMove;
    private Consumer<BoardSquare> onClickCallback;

    public BoardSquare(int squareIndex, Consumer<BoardSquare> onClickCallback) {
        super();
        this.squareIndex = squareIndex;
        this.pieceType = PieceType.EMPTY;
        this.pieceColor = Color.EMPTY;
        this.onClickCallback = onClickCallback;

        // Style
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.resetHighlight();

        // Logic
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (this.onClickCallback != null) {
                this.onClickCallback.accept(this);
            }
        });
    }

    public void highlight(String color) {
        this.setStyle("-fx-background-color: " + color);
    }

    public void resetHighlight() {
        this.setStyle("-fx-background-color: " + (BoardUtils.isLightSquare(this.squareIndex) ? "white" : "beige"));
    }

    public void setPiece(PieceType pieceType, Color pieceColor) {
        assert (pieceColor == Color.EMPTY && pieceType == PieceType.EMPTY) || (pieceColor != Color.EMPTY && pieceType != PieceType.EMPTY) : "Piece wo color / empty square w piece detected";
        this.pieceType = pieceType;
        this.pieceColor = pieceColor;
        if (pieceColor != Color.EMPTY && pieceType != PieceType.EMPTY) {
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

    public Color getPieceColor() {
        return this.pieceColor;
    }

    public PieceType getPieceType() {
        return this.pieceType;
    }

    public int getSquareIndex() {
        return this.squareIndex;
    }

    public void setTargetMove(Move move) {
        if (move == null) {
            this.resetHighlight();
        } else {
            this.highlight("green");
        }
        this.targetMove = move;
    }

    public Move getTargetMove() {
        return this.targetMove;
    }

}
