package com.superhellth.basics;

import java.util.ArrayList;
import java.util.List;

import com.superhellth.utils.BoardUtils;

public class Game {

    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String TEST_FEN = "r1bk3r/p2pBpNp/n4n2/1p1NP2P/6P1/3P4/P1P1K3/q5b1 w - - 0 1";

    private Board board;
    private MoveGenerator moveGenerator;
    private String fen;

    public Game() {
        this(Game.STARTING_FEN);
    }

    public Game(String fen) {
        this.board = new Board(fen);
        this.moveGenerator = new MoveGenerator(this.board);
        this.moveGenerator.generateAllLegalMoves();
    }

    public void executeMove(Move move) {
        this.makeMove(move);
        this.moveGenerator.generateAllLegalMoves();
    }

    /**
     * Applies a move to the board and returns undo state. Does NOT regenerate legal moves.
     */
    public MoveUndo makeMove(Move move) {
        int fromSquare = move.getFromSquare();
        int toSquare = move.getToSquare();

        Color sourceColor = this.board.getSquareColor(fromSquare);
        PieceType sourceType = this.board.getSquarePieceType(fromSquare);

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

        // Turn logic
        this.board.setActiveColor(BoardUtils.getOppositeColor(sourceColor));

        return undo;
    }

    /**
     * Reverses a move using saved undo state. Does NOT regenerate legal moves.
     */
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
    }

    public List<Move> getPseudoLegalMovesFromSquare(int squareIndex) {
        Color squareColor = this.board.getSquareColor(squareIndex);
        if (squareColor == this.board.getActiveColor()) {
            return this.moveGenerator.getAllMovesBySquareList(squareIndex);
        } else {
            return new ArrayList<>();
        }
    }

    public Board getBoard() {
        return this.board;
    }

    public MoveGenerator getMoveGenerator() {
        return this.moveGenerator;
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
}
