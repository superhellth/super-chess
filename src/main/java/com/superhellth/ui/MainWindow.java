package com.superhellth.ui;

import com.superhellth.basics.Game;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainWindow {

    private final BoardGrid boardGrid;

    public MainWindow(Game game) {
        this.boardGrid = new BoardGrid(game);
    }

    public void show(Stage stage) {
        StackPane boardPane = new StackPane(this.boardGrid);
        boardPane.setAlignment(Pos.CENTER);

        HBox root = new HBox(boardPane);
        HBox.setHgrow(boardPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 700, 500, Color.LIGHTYELLOW);

        boardGrid.prefWidthProperty().bind(
                Bindings.min(boardPane.widthProperty(), scene.heightProperty())
        );
        boardGrid.prefHeightProperty().bind(
                Bindings.min(boardPane.widthProperty(), scene.heightProperty())
        );
        boardGrid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        stage.setMaximized(true);
        stage.setTitle("Super Chess");
        stage.setScene(scene);
        stage.show();
    }
}
