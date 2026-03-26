package com.superhellth.ui;

import com.superhellth.basics.Board;

import javafx.scene.Scene;
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
        Scene scene = new Scene(this.boardGrid, 500, 500, Color.LIGHTYELLOW);
        stage.setMaximized(true);
        stage.setTitle("MyShapes with JavaFX");
        stage.setScene(scene);
        stage.show();
    }
}
