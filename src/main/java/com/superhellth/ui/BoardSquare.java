package com.superhellth.ui;

import java.util.function.Consumer;

import com.superhellth.basics.Color;
import com.superhellth.basics.Move;
import com.superhellth.basics.PieceType;
import com.superhellth.utils.BoardUtils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public class BoardSquare extends StackPane {

    private static final String LIGHT_SQUARE_COLOR = "#EBECD0";
    private static final String DARK_SQUARE_COLOR = "#739552";
    private static final String LIGHT_SQUARE_SELECTED = "#F5F682";
    private static final String DARK_SQUARE_SELECTED = "#B9CA43";

    private final int squareIndex;
    private final boolean isLight;
    private final ImageView imageView;
    private final Circle moveIndicator;
    private PieceType pieceType;
    private Color pieceColor;
    private Move targetMove;

    public BoardSquare(int squareIndex, Consumer<BoardSquare> onClickCallback) {
        super();
        this.squareIndex = squareIndex;
        this.isLight = BoardUtils.isLightSquare(squareIndex);
        this.pieceType = PieceType.EMPTY;
        this.pieceColor = Color.EMPTY;

        // Piece image
        this.imageView = new ImageView();
        this.imageView.setPreserveRatio(true);
        this.imageView.fitWidthProperty().bind(this.widthProperty().multiply(0.8));
        this.imageView.fitHeightProperty().bind(this.heightProperty().multiply(0.8));
        this.imageView.setMouseTransparent(true);

        // Move indicator circle
        this.moveIndicator = new Circle();
        this.moveIndicator.radiusProperty().bind(this.widthProperty().multiply(0.15));
        this.moveIndicator.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.25));
        this.moveIndicator.setVisible(false);
        this.moveIndicator.setMouseTransparent(true);

        this.getChildren().addAll(this.imageView, this.moveIndicator);

        // Style
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.resetHighlight();

        // Logic
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (onClickCallback != null) {
                onClickCallback.accept(this);
            }
        });
    }

    public void select() {
        this.setStyle("-fx-background-color: " + (this.isLight ? LIGHT_SQUARE_SELECTED : DARK_SQUARE_SELECTED));
    }

    public void resetHighlight() {
        this.setStyle("-fx-background-color: " + (this.isLight ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR));
        this.moveIndicator.setVisible(false);
    }

    public void showMoveIndicator() {
        this.moveIndicator.setVisible(true);
    }

    public void setPiece(PieceType pieceType, Color pieceColor) {
        assert (pieceColor == Color.EMPTY && pieceType == PieceType.EMPTY) || (pieceColor != Color.EMPTY && pieceType != PieceType.EMPTY) : "Piece wo color / empty square w piece detected";
        this.pieceType = pieceType;
        this.pieceColor = pieceColor;
        if (pieceColor != Color.EMPTY && pieceType != PieceType.EMPTY) {
            String path = "/images/" + pieceColor.name().toLowerCase()
                    + "-" + pieceType.name().toLowerCase() + ".png";
            Image image = new Image(getClass().getResourceAsStream(path), 120, 120, true, true);
            this.imageView.setImage(image);
        } else {
            this.imageView.setImage(null);
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
        this.targetMove = move;
        if (move == null) {
            this.resetHighlight();
        } else {
            this.showMoveIndicator();
        }
    }

    public Move getTargetMove() {
        return this.targetMove;
    }

}
