package com.superhellth.ui;

import java.util.Set;

import com.superhellth.basics.Color;
import com.superhellth.basics.Game;
import com.superhellth.basics.GameState;
import com.superhellth.bots.Bot;
import com.superhellth.bots.EvalBot1;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWindow {

    private Stage stage;
    private Scene scene;

    public void show(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(buildStartScreen(), 700, 500, javafx.scene.paint.Color.LIGHTYELLOW);
        stage.setMaximized(true);
        stage.setTitle("Super Chess");
        stage.setScene(this.scene);
        stage.show();
    }

    private VBox buildStartScreen() {
        Label title = new Label("Super Chess");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        Label prompt = new Label("Choose how to play:");
        prompt.setStyle("-fx-font-size: 18px;");

        Button bothBtn = new Button("Play Both Colors");
        Button whiteBtn = new Button("Play as White (vs Bot)");
        Button blackBtn = new Button("Play as Black (vs Bot)");

        String btnStyle = "-fx-font-size: 16px; -fx-min-width: 260px; -fx-padding: 10 20;";
        bothBtn.setStyle(btnStyle);
        whiteBtn.setStyle(btnStyle);
        blackBtn.setStyle(btnStyle);

        bothBtn.setOnAction(e -> startGame(BoardGrid.bothColors(), null, Color.WHITE));
        whiteBtn.setOnAction(e -> startGame(BoardGrid.onlyColor(Color.WHITE), new EvalBot1(3), Color.WHITE));
        blackBtn.setOnAction(e -> startGame(BoardGrid.onlyColor(Color.BLACK), new EvalBot1(3), Color.BLACK));

        VBox box = new VBox(20, title, prompt, bothBtn, whiteBtn, blackBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        return box;
    }

    private void startGame(Set<Color> humanColors, Bot bot, Color orientation) {
        Game game = new Game();
        BoardGrid boardGrid = new BoardGrid(game, humanColors, bot, orientation);

        StackPane boardPane = new StackPane(boardGrid);
        boardPane.setAlignment(Pos.CENTER);

        Button newGameBtn = new Button("New Game");
        newGameBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 16;");
        newGameBtn.setOnAction(e -> this.scene.setRoot(buildStartScreen()));

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-wrap-text: true;");
        statusLabel.setMaxWidth(160);

        Label turnLabel = new Label();
        turnLabel.setStyle("-fx-font-size: 14px;");

        boardGrid.setOnStateChange(() -> {
            GameState state = boardGrid.getGameState();
            switch (state) {
                case WHITE_WINS_BY_CHECKMATE -> {
                    statusLabel.setText("White wins!");
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a7a1a;");
                    turnLabel.setText("Checkmate");
                }
                case BLACK_WINS_BY_CHECKMATE -> {
                    statusLabel.setText("Black wins!");
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a7a1a;");
                    turnLabel.setText("Checkmate");
                }
                case DRAW_BY_FIFTY_MOVE_RULE -> {
                    statusLabel.setText("Draw");
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #888;");
                    turnLabel.setText("");
                }
                case DRAW_BY_INSUFFICIENT_MATERIAL -> {
                    statusLabel.setText("Draw");
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #888;");
                    turnLabel.setText("");
                }
                case DRAW_BY_MOVE_LIMIT -> {
                    statusLabel.setText("Draw");
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #888;");
                    turnLabel.setText("");
                }
                case DRAW_BY_REPETITION -> {
                    statusLabel.setText("Draw");
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #888;");
                    turnLabel.setText("");
                }
                case DRAW_BY_STALEMATE -> {
                    statusLabel.setText("Draw");
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #888;");
                    turnLabel.setText("Stalemate");
                }
                default -> {
                    statusLabel.setText("In progress");
                    statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
                    turnLabel.setText((boardGrid.getActiveColor() == Color.WHITE ? "White" : "Black") + " to move");
                }
            }
        });

        VBox sidePanel = new VBox(10, statusLabel, turnLabel, newGameBtn);
        sidePanel.setAlignment(Pos.TOP_CENTER);
        sidePanel.setPadding(new Insets(20));
        sidePanel.setMinWidth(180);

        HBox root = new HBox(boardPane, sidePanel);
        HBox.setHgrow(boardPane, Priority.ALWAYS);

        boardGrid.prefWidthProperty().bind(
                Bindings.min(boardPane.widthProperty(), this.scene.heightProperty())
        );
        boardGrid.prefHeightProperty().bind(
                Bindings.min(boardPane.widthProperty(), this.scene.heightProperty())
        );
        boardGrid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        this.scene.setRoot(root);
    }
}
