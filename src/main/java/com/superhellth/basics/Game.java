package com.superhellth.basics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.superhellth.utils.BoardUtils;

public class Game {

    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private Board board;
    private GameState gameState;
    private MoveGenerator moveGenerator;
    private int moveCount = 0;
    private final Map<String, Integer> positionHistory = new HashMap<>();

    public Game() {
        this(Game.STARTING_FEN);
    }

    public Game(String fen) {
        this.board = new Board(fen);
        this.gameState = GameState.ONGOING;
        this.moveGenerator = new MoveGenerator(this.board);
        this.moveGenerator.generateLegalMoves();
        this.recordPosition();
    }

    public MoveUndo makeMove(Move move) {
        int fromSquare = move.getFromSquare();
        int toSquare = move.getToSquare();

        Color sourceColor = this.board.getSquareColor(fromSquare);
        PieceType sourceType = this.board.getSquarePieceType(fromSquare);
        assert sourceColor != Color.EMPTY || sourceType != PieceType.EMPTY : "No piece at fromSquare, square: " + fromSquare;

        // Save undo state
        PieceType capturedType = this.board.getSquarePieceType(toSquare);
        Color capturedColor = this.board.getSquareColor(toSquare);
        MoveUndo undo = new MoveUndo(
                capturedType, capturedColor,
                this.board.getCastlingRightsRaw().clone(),
                this.board.getEnPassantSquare(),
                this.board.getHalfmoveClock()
        );

        // Move pieces
        this.board.removePiece(fromSquare);
        this.board.removePiece(toSquare);
        this.board.placePiece(sourceColor, sourceType, toSquare);

        // Handle castling rights + rook movement
        this.updateCastlingRights(fromSquare, toSquare, sourceColor, sourceType);
        if (sourceType == PieceType.KING && Math.abs(toSquare - fromSquare) == 2) {
            boolean kingside = toSquare > fromSquare;
            int rookFrom = kingside ? fromSquare + 3 : fromSquare - 4;
            int rookTo = kingside ? fromSquare + 1 : fromSquare - 1;
            this.board.removePiece(rookFrom);
            this.board.placePiece(sourceColor, PieceType.ROOK, rookTo);
        }

        // Handle en passant capture
        if (sourceType == PieceType.PAWN && toSquare == this.board.getEnPassantSquare()) {
            int capturedPawnSquare = sourceColor == Color.WHITE ? toSquare - 8 : toSquare + 8;
            this.board.removePiece(capturedPawnSquare);
        }

        // Handle en passant double push
        if (sourceType == PieceType.PAWN && Math.abs(toSquare - fromSquare) == 16) {
            int enPassantSquare = (fromSquare + toSquare) / 2;
            this.board.setEnPassantSquare(enPassantSquare);
        } else {
            this.board.setEnPassantSquare(-1);
        }

        // Handle promotion
        if (move.getPromotionPieceType() != PieceType.EMPTY) {
            this.board.removePiece(toSquare);
            this.board.placePiece(sourceColor, move.getPromotionPieceType(), toSquare);
        }

        // Halfmove clock: reset on pawn move or capture, otherwise increment
        if (sourceType == PieceType.PAWN || capturedType != PieceType.EMPTY) {
            this.board.setHalfmoveClock(0);
        } else {
            this.board.setHalfmoveClock(this.board.getHalfmoveClock() + 1);
        }

        // Turn logic
        this.board.setActiveColor(BoardUtils.getOppositeColor(sourceColor));

        this.moveGenerator.generateLegalMoves();
        this.moveCount++;
        this.recordPosition();

        // Game state check
        if (this.moveGenerator.getLegalMoves().isEmpty()) {
            if ((this.board.getPieceBitboard(this.board.getActiveColor(), PieceType.KING) & this.moveGenerator.getAttackBitboardByColor(sourceColor)) != 0) {
                this.gameState = sourceColor == Color.WHITE ? GameState.WHITE_WINS_BY_CHECKMATE : GameState.BLACK_WINS_BY_CHECKMATE;
            } else {
                this.gameState = GameState.DRAW_BY_STALEMATE;
            }
        } else if (this.getPositionCount() >= 3) {
            this.gameState = GameState.DRAW_BY_REPETITION;
        } else if (this.board.getHalfmoveClock() >= 100) {
            this.gameState = GameState.DRAW_BY_FIFTY_MOVE_RULE;
        } else if (this.isInsufficientMaterial()) {
            this.gameState = GameState.DRAW_BY_INSUFFICIENT_MATERIAL;
        } else if (this.moveCount >= 200) {
            this.gameState = GameState.DRAW_BY_MOVE_LIMIT;
        }

        return undo;
    }


    public void undoMove(Move move, MoveUndo undo) {
        int fromSquare = move.getFromSquare();
        int toSquare = move.getToSquare();

        // The piece that moved is now at toSquare (or promoted piece)
        Color sourceColor = this.board.getSquareColor(toSquare);
        PieceType movedType = this.board.getSquarePieceType(toSquare);

        // If promotion, the original piece was a pawn
        PieceType originalType = move.getPromotionPieceType() != PieceType.EMPTY ? PieceType.PAWN : movedType;

        // Remove piece from destination
        this.board.removePiece(toSquare);

        // Place piece back at origin
        this.board.placePiece(sourceColor, originalType, fromSquare);

        // Restore captured piece (normal capture)
        if (undo.capturedType != PieceType.EMPTY) {
            this.board.placePiece(undo.capturedColor, undo.capturedType, toSquare);
        }

        // Undo en passant capture
        if (originalType == PieceType.PAWN && toSquare == undo.enPassantSquare && undo.enPassantSquare != -1) {
            Color opponentColor = BoardUtils.getOppositeColor(sourceColor);
            int capturedPawnSquare = sourceColor == Color.WHITE ? toSquare - 8 : toSquare + 8;
            this.board.placePiece(opponentColor, PieceType.PAWN, capturedPawnSquare);
        }

        // Undo castling rook movement
        if (originalType == PieceType.KING && Math.abs(toSquare - fromSquare) == 2) {
            boolean kingside = toSquare > fromSquare;
            int rookFrom = kingside ? fromSquare + 3 : fromSquare - 4;
            int rookTo = kingside ? fromSquare + 1 : fromSquare - 1;
            this.board.removePiece(rookTo);
            this.board.placePiece(sourceColor, PieceType.ROOK, rookFrom);
        }

        // Restore state
        this.board.setCastlingRightsRaw(undo.castlingRights);
        this.board.setEnPassantSquare(undo.enPassantSquare);
        this.board.setHalfmoveClock(undo.halfmoveClock);
        this.board.setActiveColor(sourceColor);

        // Undo position history
        String key = this.getPositionKey();
        int count = this.positionHistory.getOrDefault(key, 0);
        if (count <= 1) {
            this.positionHistory.remove(key);
        } else {
            this.positionHistory.put(key, count - 1);
        }
        this.moveCount--;
        this.gameState = GameState.ONGOING;

        this.moveGenerator.generateLegalMoves();
    }

    public Board getBoard() {
        return this.board;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public MoveGenerator getMoveGenerator() {
        return this.moveGenerator;
    }

    public List<Move> getLegalMoves() {
        return this.moveGenerator.getLegalMoves();
    }


    // Rook home squares: white kingside=7(h1), white queenside=0(a1), black kingside=63(h8), black queenside=56(a8)
    private static final int[] ROOK_HOME_SQUARES = {7, 0, 63, 56};

    private void updateCastlingRights(int fromSquare, int toSquare, Color sourceColor, PieceType sourceType) {
        // King moves -> lose both sides
        if (sourceType == PieceType.KING) {
            this.board.revokeCastlingRight(sourceColor, 0);
            this.board.revokeCastlingRight(sourceColor, 1);
            return;
        }

        // Rook leaves home square -> lose that side
        if (sourceType == PieceType.ROOK) {
            for (int side = 0; side < 2; side++) {
                if (fromSquare == ROOK_HOME_SQUARES[sourceColor.ordinal() * 2 + side]) {
                    this.board.revokeCastlingRight(sourceColor, side);
                }
            }
        }

        // Capture on opponent's rook home square -> opponent loses that side
        Color opponentColor = BoardUtils.getOppositeColor(sourceColor);
        for (int side = 0; side < 2; side++) {
            if (toSquare == ROOK_HOME_SQUARES[opponentColor.ordinal() * 2 + side]) {
                this.board.revokeCastlingRight(opponentColor, side);
            }
        }
    }

    /**
     * Returns position key for repetition detection.
     * Uses FEN without halfmove clock and fullmove number.
     */
    private String getPositionKey() {
        String fen = this.board.toFEN();
        // Strip last two fields (halfmove clock, fullmove number)
        int lastSpace = fen.lastIndexOf(' ');
        int secondLastSpace = fen.lastIndexOf(' ', lastSpace - 1);
        return fen.substring(0, secondLastSpace);
    }

    private void recordPosition() {
        String key = this.getPositionKey();
        this.positionHistory.merge(key, 1, Integer::sum);
    }

    private int getPositionCount() {
        return this.positionHistory.getOrDefault(this.getPositionKey(), 0);
    }

    private boolean isInsufficientMaterial() {
        // Count all pieces (excluding kings)
        for (PieceType type : new PieceType[]{PieceType.PAWN, PieceType.ROOK, PieceType.QUEEN}) {
            if (this.board.getPieceBitboard(Color.WHITE, type) != 0 || this.board.getPieceBitboard(Color.BLACK, type) != 0) {
                return false;
            }
        }

        long whiteKnights = this.board.getPieceBitboard(Color.WHITE, PieceType.KNIGHT);
        long blackKnights = this.board.getPieceBitboard(Color.BLACK, PieceType.KNIGHT);
        long whiteBishops = this.board.getPieceBitboard(Color.WHITE, PieceType.BISHOP);
        long blackBishops = this.board.getPieceBitboard(Color.BLACK, PieceType.BISHOP);

        int whiteMinors = Long.bitCount(whiteKnights) + Long.bitCount(whiteBishops);
        int blackMinors = Long.bitCount(blackKnights) + Long.bitCount(blackBishops);

        // K vs K
        if (whiteMinors == 0 && blackMinors == 0) return true;
        // K+minor vs K
        if ((whiteMinors == 1 && blackMinors == 0) || (whiteMinors == 0 && blackMinors == 1)) return true;
        // K+B vs K+B (same color bishops)
        if (whiteMinors == 1 && blackMinors == 1
                && Long.bitCount(whiteBishops) == 1 && Long.bitCount(blackBishops) == 1) {
            int wbSquare = Long.numberOfTrailingZeros(whiteBishops);
            int bbSquare = Long.numberOfTrailingZeros(blackBishops);
            // Same color = same parity of (rank + file)
            return ((wbSquare / 8 + wbSquare % 8) % 2) == ((bbSquare / 8 + bbSquare % 8) % 2);
        }

        return false;
    }
}
