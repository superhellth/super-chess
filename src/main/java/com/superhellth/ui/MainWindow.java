package com.superhellth.ui;

import java.util.Map;

import com.superhellth.basics.Board;
import com.superhellth.basics.MoveGenerator;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainWindow {

    private final Board board;
    private final MoveGenerator moveGenerator;
    private final BoardGrid boardGrid;

    public MainWindow(Board board) {
        this.board = board;
        this.moveGenerator = new MoveGenerator(board);
        this.boardGrid = new BoardGrid(board);
    }

    public void show(Stage stage) {
        StackPane boardPane = new StackPane(this.boardGrid);
        boardPane.setAlignment(Pos.CENTER);

        Map<String, Long> namedBitboards = board.getNamedBitboards();
        namedBitboards.put("White Pawn Push Targets", moveGenerator.getPawnPushTargets(com.superhellth.basics.Color.WHITE));
        namedBitboards.put("Black Pawn Push Targets", moveGenerator.getPawnPushTargets(com.superhellth.basics.Color.BLACK));

        ObservableList<String> items = FXCollections.observableArrayList(namedBitboards.keySet());

        ListView<String> bitboardList = new ListView<>(items);
        bitboardList.setPrefWidth(200);
        bitboardList.setMaxWidth(200);

        bitboardList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boardGrid.visualizeBitboard(namedBitboards.get(newVal));
            } else {
                boardGrid.visualizeBitboard(0L);
            }
        });

        HBox root = new HBox(boardPane, bitboardList);
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
