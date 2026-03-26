package com.superhellth.ui;

import com.superhellth.basics.Board;

import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainWindow {

    private final Board board;
    private final BoardGrid boardGrid;

    public MainWindow(Board board) {
        this.board = board;
        this.boardGrid = new BoardGrid(board);
    }

    public void show(Stage stage) {
        StackPane root = new StackPane(this.boardGrid);
        Scene scene = new Scene(root, 500, 500, Color.LIGHTYELLOW);

        // Make the board square and scale with the smaller scene dimension
        boardGrid.prefWidthProperty().bind(
                Bindings.min(scene.widthProperty(), scene.heightProperty())
        );
        boardGrid.prefHeightProperty().bind(
                Bindings.min(scene.widthProperty(), scene.heightProperty())
        );
        boardGrid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        stage.setMaximized(true);
        stage.setTitle("Super Chess");
        stage.setScene(scene);
        stage.show();
    }
}
