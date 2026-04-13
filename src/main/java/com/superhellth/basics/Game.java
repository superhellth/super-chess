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
        int fromSquare = move.getFromSquare();
        int toSquare = move.getToSquare();

        Color sourceColor = this.board.getSquareColor(fromSquare);
        assert sourceColor == this.board.getActiveColor() : "Executing move of inactive color!";
        PieceType sourceType = this.board.getSquarePieceType(fromSquare);
        assert (sourceColor != Color.EMPTY) && (sourceType != PieceType.EMPTY) : "Empty piece / color making a move!";

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
        this.moveGenerator.generateAllLegalMoves();
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
