package com.superhellth.basics;

import java.util.LinkedHashMap;
import java.util.Map;

public class Board {

    private static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final String TEST_FEN = "8/8/8/2k5/4K3/8/8/8 w - - 0 1";
    private Color activeColor;
    private long[][] bitboards;
    private boolean[] castlingRights; // [white kingside, white queenside, black kingside, black queenside]
    private int enPassantSquare; // -1 if no en passant square
    private int halfmoveClock;
    private int fullmoveNumber;

    public Board() {
        this.activeColor = Color.WHITE;
        this.bitboards = new long[3][6];
        this.castlingRights = new boolean[4];
        this.enPassantSquare = -1;
        this.halfmoveClock = 0;
        this.fullmoveNumber = 1;
        this.bitboards[2][0] = -1L;

        this.loadFromFEN(Board.TEST_FEN);
    }

    private void placePiece(Color color, PieceType pieceType, int square) {
        this.bitboards[color.ordinal()][pieceType.ordinal()] |= (1L << square);
        this.bitboards[2][0] &= ~(1L << square);
    }

    public void resetBoard() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                this.bitboards[i][j] = 0L;
            }
        }
        this.bitboards[2][0] = -1L;
        this.activeColor = Color.WHITE;
        this.castlingRights = new boolean[4];
        this.enPassantSquare = -1;
        this.halfmoveClock = 0;
        this.fullmoveNumber = 1;
    }

    public void loadFromFEN(String fen) {
        this.resetBoard();

        int boardIndex = 56;
        int runningIndex = 0;
        char currentChar = fen.charAt(runningIndex);
        while (currentChar != ' ') {
            // Skip squares
            if (Character.isDigit(currentChar)) {
                int emptySquares = Integer.parseInt(String.valueOf(currentChar));
                boardIndex += emptySquares;

                // Place piece
            } else if (Character.isLetter(currentChar)) {
                Color color = Character.isUpperCase(currentChar) ? Color.WHITE : Color.BLACK;
                PieceType pieceType;
                switch (Character.toLowerCase(currentChar)) {
                    case 'p' ->
                        pieceType = PieceType.PAWN;
                    case 'n' ->
                        pieceType = PieceType.KNIGHT;
                    case 'b' ->
                        pieceType = PieceType.BISHOP;
                    case 'r' ->
                        pieceType = PieceType.ROOK;
                    case 'q' ->
                        pieceType = PieceType.QUEEN;
                    case 'k' ->
                        pieceType = PieceType.KING;
                    default ->
                        throw new IllegalArgumentException("Invalid FEN character: " + currentChar);
                }
                this.placePiece(color, pieceType, boardIndex);
                boardIndex++;

                // Move to next rank
            } else if (currentChar == '/') {
                boardIndex -= 16;
            } else {
                throw new IllegalArgumentException("Invalid FEN character: " + currentChar);
            }
            currentChar = fen.charAt(++runningIndex);
        }

        // Active color
        currentChar = fen.charAt(++runningIndex);
        this.activeColor = currentChar == 'w' ? Color.WHITE : Color.BLACK;

        // Castling rights
        runningIndex += 2;
        currentChar = fen.charAt(runningIndex);
        while (currentChar != ' ') {
            switch (currentChar) {
                case 'K' ->
                    castlingRights[0] = true;
                case 'Q' ->
                    castlingRights[1] = true;
                case 'k' ->
                    castlingRights[2] = true;
                case 'q' ->
                    castlingRights[3] = true;
                case '-' -> {
                }
                default ->
                    throw new IllegalArgumentException("Invalid FEN character: " + currentChar);
            }
            currentChar = fen.charAt(++runningIndex);
        }

        // En passant
        runningIndex++;
        currentChar = fen.charAt(runningIndex);
        if (currentChar != '-') {
            int file = currentChar - 'a';
            int rank = fen.charAt(runningIndex + 1) - '1';
            this.enPassantSquare = rank * 8 + file;
        } else {
            this.enPassantSquare = -1;
        }

        // Halfmove clock
        runningIndex += 2;
        int halfmoveStart = runningIndex;
        while (fen.charAt(runningIndex) != ' ') {
            runningIndex++;
        }
        this.halfmoveClock = Integer.parseInt(fen.substring(halfmoveStart, runningIndex));

        // Fullmove number
        runningIndex++;
        int fullmoveStart = runningIndex;
        while (runningIndex < fen.length() && fen.charAt(runningIndex) != ' ') {
            runningIndex++;
        }
        this.fullmoveNumber = Integer.parseInt(fen.substring(fullmoveStart, runningIndex));
    }

    public Map<String, Long> getNamedBitboards() {
        Map<String, Long> named = new LinkedHashMap<>();
        for (Color color : Color.values()) {
            for (PieceType pieceType : PieceType.values()) {
                if (pieceType == PieceType.EMPTY) {
                    continue;
                }
                long bitboard = this.bitboards[color.ordinal()][pieceType.ordinal()];
                named.put(color.name() + " " + pieceType.name(), bitboard);
            }
        }
        named.put("Empty", this.bitboards[2][0]);
        named.put("NONE", 0L);
        return named;
    }

    public long getBitboard(Color color, PieceType pieceType) {
        if (pieceType == PieceType.EMPTY) {
            return this.bitboards[2][0];
        }
        return this.bitboards[color.ordinal()][pieceType.ordinal()];
    }

}
