package com.superhellth.ui;

import java.util.Map;

import com.superhellth.basics.Board;
import com.superhellth.basics.Direction;
import com.superhellth.basics.Game;
import com.superhellth.basics.PseudoLegalMoveGenerator;
import com.superhellth.basics.PseudoLegalMoveProvider;

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
    private final PseudoLegalMoveGenerator moveGenerator;
    private final PseudoLegalMoveProvider moveProvider;
    private final BoardGrid boardGrid;

    public MainWindow(Game game) {
        this.board = game.getBoard();
        this.moveGenerator = game.getMoveGenerator();
        this.moveProvider = game.getMoveProvider();
        this.boardGrid = new BoardGrid(this.board, this.moveProvider);
    }

    public void show(Stage stage) {
        StackPane boardPane = new StackPane(this.boardGrid);
        boardPane.setAlignment(Pos.CENTER);

        // Register bitboards
        Map<String, Long> namedBitboards = board.getNamedBitboards();
        for (com.superhellth.basics.Color color : com.superhellth.basics.Color.values()) {
            if (color == com.superhellth.basics.Color.EMPTY) {
                continue;
            }
            long[] pawnPushTargets = this.moveGenerator.getPawnPushTargets(color);
            namedBitboards.put(color + " Pawn Single Push Targets", pawnPushTargets[0]);
            namedBitboards.put(color + " Pawn Double Push Targets", pawnPushTargets[1]);
            namedBitboards.put(color + " Pawn Attack Targets East", this.moveGenerator.getPawnAttackTargets(color, Direction.EAST));
            namedBitboards.put(color + " Pawn Attack Targets West", this.moveGenerator.getPawnAttackTargets(color, Direction.WEST));
        }
        namedBitboards.put("None", 0L);

        // Display bitboards
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

        // Display shift options
        Map<String, Direction> shiftDirections = Map.of(
                "Shift North", Direction.NORTH,
                "Shift South", Direction.SOUTH,
                "Shift East", Direction.EAST,
                "Shift West", Direction.WEST,
                "Shift North-East", Direction.NORTH_EAST,
                "Shift North-West", Direction.NORTH_WEST,
                "Shift South-East", Direction.SOUTH_EAST,
                "Shift South-West", Direction.SOUTH_WEST
        );
        ObservableList<String> shifts = FXCollections.observableArrayList(shiftDirections.keySet());
        ListView<String> shiftList = new ListView<>(shifts);
        shiftList.setPrefWidth(200);
        shiftList.setMaxWidth(200);
        shiftList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boardGrid.shiftHighlightedBitboard(shiftDirections.get(newVal));
            } else {
                boardGrid.visualizeBitboard(0L);
            }
        });

        HBox root = new HBox(shiftList, boardPane, bitboardList);
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
