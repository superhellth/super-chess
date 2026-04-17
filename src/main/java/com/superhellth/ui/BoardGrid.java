package com.superhellth.ui;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.superhellth.basics.Board;
import com.superhellth.basics.Color;
import com.superhellth.basics.Game;
import com.superhellth.basics.GameState;
import com.superhellth.basics.Move;
import com.superhellth.basics.MoveGenerator;
import com.superhellth.basics.PieceType;
import com.superhellth.bots.Bot;
import com.superhellth.utils.BoardUtils;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.util.Duration;

public class BoardGrid extends GridPane {

    private final Game game;
    private final Board board;
    private final BoardSquare[] squares = new BoardSquare[64];
    private final MoveGenerator moveGenerator;
    private final Set<Color> humanColors;
    private final Bot bot;
    private final Color orientation;
    private Runnable onStateChange;
    private BoardSquare selectedSquare = null;
    private Move pendingPromotionMove = null;
    private int[] promotionOptionSquares = null;

    public BoardGrid(Game game, Set<Color> humanColors, Bot bot, Color orientation) {
        super();
        this.game = game;
        this.board = game.getBoard();
        this.moveGenerator = game.getMoveGenerator();
        this.humanColors = humanColors;
        this.bot = bot;
        this.orientation = orientation;

        if (bot != null) {
            bot.setup(this.game);
        }

        // Initialize squares
        for (int squareIndex = 0; squareIndex < 64; squareIndex++) {
            this.squares[squareIndex] = new BoardSquare(squareIndex, this::handleSquareClick);
            int[] rankAndFile = BoardUtils.getRankAndFileFromSquareIndex(squareIndex);
            int rank = rankAndFile[0];
            int file = rankAndFile[1];
            int col = orientation == Color.WHITE ? file : 7 - file;
            int row = orientation == Color.WHITE ? 7 - rank : rank;
            this.add(this.squares[squareIndex], col, row);
        }
        this.loadBoard();

        // Styling
        for (int i = 0; i < 8; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(12.5);
            col.setFillWidth(true);
            getColumnConstraints().add(col);

            RowConstraints row = new RowConstraints();
            row.setPercentHeight(12.5);
            row.setFillHeight(true);
            getRowConstraints().add(row);
        }

        // Trigger bot if bot starts
        maybeTriggerBot();
    }

    private void loadBoard() {
        for (int i = 0; i < 64; i++) {
            PieceType pieceType = this.board.getSquarePieceType(i);
            Color pieceColor = this.board.getSquareColor(i);
            this.squares[i].setPiece(pieceType, pieceColor);
            this.squares[i].resetHighlight();
            this.squares[i].setTargetMove(null, false);
        }
    }

    private boolean isHumanTurn() {
        return this.humanColors.contains(this.board.getActiveColor());
    }

    private void handleSquareClick(BoardSquare square) {
        // Block input if bot's turn or game ended
        if (this.game.getGameState() != GameState.ONGOING) {
            return;
        }
        if (this.pendingPromotionMove == null && !isHumanTurn()) {
            return;
        }

        // Handle click during promotion selection
        if (this.pendingPromotionMove != null) {
            PieceType chosenPiece = square.getPromotionOption();
            clearPromotionOptions();
            if (chosenPiece != null) {
                this.pendingPromotionMove.setPromotionPieceType(chosenPiece);
                this.game.makeMove(this.pendingPromotionMove);
            }
            this.pendingPromotionMove = null;
            this.selectedSquare = null;
            this.loadBoard();
            notifyStateChange();
            maybeTriggerBot();
            return;
        }

        int squareIndex = square.getSquareIndex();
        Move squareTargetMove = square.getTargetMove();

        if (this.selectedSquare != null) {
            this.selectedSquare.resetHighlight();
            this.resetAllTargetMoves();
        }

        if (this.selectedSquare == square) {
            this.selectedSquare = null;
        } else if (squareTargetMove != null) {
            if (square.isTargetMovePromotion()) {
                showPromotionOptions(squareTargetMove);
            } else {
                this.game.makeMove(squareTargetMove);
                this.selectedSquare = null;
                this.loadBoard();
                this.notifyStateChange();
                maybeTriggerBot();
            }
        } else {
            this.selectedSquare = square;
            for (Move move : this.getLegalMovesFromSquare(squareIndex)) {
                int targetSquareIndex = move.getToSquare();
                boolean isCapture = this.board.getSquareColor(targetSquareIndex) != Color.EMPTY;
                this.squares[targetSquareIndex].setTargetMove(move, isCapture);
                this.squares[targetSquareIndex].setTargetMoveIsPromotion(move.getPromotionPieceType() != PieceType.EMPTY);
            }
            square.select();
        }
    }

    private void maybeTriggerBot() {
        if (this.bot == null) return;
        if (this.game.getGameState() != GameState.ONGOING) return;
        if (isHumanTurn()) return;

        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> {
            if (this.game.getGameState() != GameState.ONGOING) return;
            if (isHumanTurn()) return;
            List<Move> legal = this.game.getLegalMoves();
            if (legal.isEmpty()) return;
            Move botMove = this.bot.selectMove();
            this.game.makeMove(botMove);
            this.loadBoard();
            this.notifyStateChange();
            Platform.runLater(this::maybeTriggerBot);
        });
        pause.play();
    }

    private void showPromotionOptions(Move move) {
        this.pendingPromotionMove = move;
        if (this.selectedSquare != null) {
            this.selectedSquare.resetHighlight();
        }
        this.resetAllTargetMoves();

        int targetSquare = move.getToSquare();
        int file = targetSquare % 8;
        int rank = targetSquare / 8;
        Color pieceColor = this.board.getSquareColor(move.getFromSquare());

        // White promotes on rank 7 -> extend downward; black promotes on rank 0 -> extend upward
        int rankStep = (rank == 7) ? -1 : 1;

        PieceType[] options = { PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT };
        this.promotionOptionSquares = new int[4];

        for (int i = 0; i < 4; i++) {
            int optionRank = rank + i * rankStep;
            int squareIndex = optionRank * 8 + file;
            this.promotionOptionSquares[i] = squareIndex;
            this.squares[squareIndex].showPromotionOption(options[i], pieceColor);
        }
    }

    private void clearPromotionOptions() {
        if (this.promotionOptionSquares != null) {
            for (int squareIndex : this.promotionOptionSquares) {
                this.squares[squareIndex].clearPromotionOption();
            }
            this.promotionOptionSquares = null;
        }
    }

    private List<Move> getLegalMovesFromSquare(int squareIndex) {
        Color squareColor = this.board.getSquareColor(squareIndex);
        if (squareColor == this.board.getActiveColor()) {
            return this.moveGenerator.getAllMovesBySquareList(squareIndex);
        } else {
            return new ArrayList<>();
        }
    }

    private void resetAllTargetMoves() {
        for (int i = 0; i < 64; i++) {
            this.squares[i].setTargetMove(null, false);
        }
    }

    public GameState getGameState() {
        return this.game.getGameState();
    }

    public Color getActiveColor() {
        return this.board.getActiveColor();
    }

    public void setOnStateChange(Runnable callback) {
        this.onStateChange = callback;
        if (callback != null) callback.run();
    }

    private void notifyStateChange() {
        if (this.onStateChange != null) this.onStateChange.run();
    }

    public static Set<Color> bothColors() {
        return EnumSet.of(Color.WHITE, Color.BLACK);
    }

    public static Set<Color> onlyColor(Color c) {
        return EnumSet.of(c);
    }

}
