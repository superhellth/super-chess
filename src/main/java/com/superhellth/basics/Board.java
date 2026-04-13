package com.superhellth.basics;

import com.superhellth.utils.BoardUtils;

public class Board {

    private final long[][] pieceBitboards;
    private final long[] occupancyBitboards; // [white, black, empty]
    private final Color[] squareColors; // Precomputed colors for each square
    private final PieceType[] squarePieceTypes; // Precomputed piece types for each square
    private Color activeColor;
    private boolean[] castlingRights; // [white kingside, white queenside, black kingside, black queenside]
    private int enPassantSquare; // -1 if no en passant square
    private int halfmoveClock;
    private int fullmoveNumber;

    public Board(String startingFEN) {
        this.activeColor = Color.WHITE;
        this.pieceBitboards = new long[2][6];
        this.occupancyBitboards = new long[3];
        this.squareColors = new Color[64];
        this.squarePieceTypes = new PieceType[64];

        this.castlingRights = new boolean[4];

        this.resetBoard();
        this.loadFromFEN(startingFEN);
    }

    public void placePiece(Color color, PieceType pieceType, int square) {
        this.pieceBitboards[color.ordinal()][pieceType.ordinal()] |= (1L << square);
        this.occupancyBitboards[color.ordinal()] |= (1L << square);
        this.occupancyBitboards[Color.EMPTY.ordinal()] &= ~(1L << square);
        this.squareColors[square] = color;
        this.squarePieceTypes[square] = pieceType;
    }

    public void removePiece(int square) {
        Color color = this.squareColors[square];
        PieceType pieceType = this.squarePieceTypes[square];
        if ((color == Color.EMPTY && pieceType != PieceType.EMPTY) || (color != Color.EMPTY && pieceType == PieceType.EMPTY)) {
            throw new RuntimeException("Piece on empty field / Piece without color!");
        }
        if (color == Color.EMPTY && pieceType == PieceType.EMPTY) {
            return;
        }
        this.pieceBitboards[color.ordinal()][pieceType.ordinal()] &= ~(1L << square);
        this.occupancyBitboards[color.ordinal()] &= ~(1L << square);
        this.occupancyBitboards[Color.EMPTY.ordinal()] |= (1L << square);
        this.squareColors[square] = Color.EMPTY;
        this.squarePieceTypes[square] = PieceType.EMPTY;
    }

    public void resetBoard() {
        // shouldnt i just recreate the bitboards here?
        for (int i = 0; i < 2; i++) {
            this.occupancyBitboards[i] = 0L;
            for (int j = 0; j < 6; j++) {
                this.pieceBitboards[i][j] = 0L;
            }
        }
        this.occupancyBitboards[2] = -1L;
        for (int i = 0; i < 64; i++) {
            this.squareColors[i] = Color.EMPTY;
            this.squarePieceTypes[i] = PieceType.EMPTY;
        }
        this.activeColor = Color.WHITE;
        this.castlingRights = new boolean[4];
        this.enPassantSquare = -1;
        this.halfmoveClock = 0;
        this.fullmoveNumber = 1;
    }

    public long getOccupancyBitboard(Color color) {
        return this.occupancyBitboards[color.ordinal()];
    }

    public long getPieceBitboard(Color color, PieceType pieceType) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when getting piece bitboard";
        if (pieceType == PieceType.EMPTY) {
            return ~this.occupancyBitboards[color.ordinal()];
        }
        return this.pieceBitboards[color.ordinal()][pieceType.ordinal()];
    }

    public Color getSquareColor(int square) {
        return this.squareColors[square];
    }

    public PieceType getSquarePieceType(int square) {
        return this.squarePieceTypes[square];
    }

    // 0 kingside, 1 queenside
    public boolean[] getCastlingRights(Color color) {
        assert color != Color.EMPTY : "EMPTY has no castlig rights";
        return color == Color.WHITE ? new boolean[]{this.castlingRights[0], this.castlingRights[1]} : new boolean[]{this.castlingRights[2], this.castlingRights[3]};
    }

    // 0 kingside, 1 queenside
    public void revokeCastlingRight(Color color, int side) {
        assert color != Color.EMPTY : "EMPTY has no castlig rights";
        this.castlingRights[color == Color.WHITE ? side : 2 + side] = false;
    }

    public int getEnPassantSquare() {
        return this.enPassantSquare;
    }

    public void setEnPassantSquare(int square) {
        this.enPassantSquare = square;
    }

    public Color getActiveColor() {
        return this.activeColor;
    }

    public void setActiveColor(Color color) {
        this.activeColor = color;
    }

    public String toFEN() {
        int boardIndex = 56;
        int fileIndex = 0;
        String fen = "";

        // Piece setup
        for (int rank = 7; rank >= 0; rank--) {
            int emptyFiles = 0;
            for (int file = 0; file < 8; file++) {
                int squareIndex = BoardUtils.getSquareIndexFromRankAndFile(rank, file);
                Color color = this.squareColors[squareIndex];
                PieceType type = this.squarePieceTypes[squareIndex];

                assert (color == Color.EMPTY && type == PieceType.EMPTY) || (color != Color.EMPTY && type == PieceType.EMPTY) : "Inconsistent piece detected";
                if (type == PieceType.EMPTY) {
                    emptyFiles++;
                } else {
                    if (emptyFiles != 0) {
                        fen += emptyFiles;
                    }
                    String pieceStr = "";
                    switch (type) {
                        case PAWN ->
                            pieceStr = "p";
                        case KNIGHT ->
                            pieceStr = "n";
                        case BISHOP ->
                            pieceStr = "b";
                        case ROOK ->
                            pieceStr = "r";
                        case QUEEN ->
                            pieceStr = "q";
                        case KING ->
                            pieceStr = "k";
                    }
                    pieceStr = color == Color.WHITE ? pieceStr.toUpperCase() : pieceStr;
                    fen += pieceStr;
                    emptyFiles = 0;
                }
            }
            if (emptyFiles != 0) {
                fen += emptyFiles;
            }
            if (rank != 0) {
                fen += "/";
            }
        }

        // Active color
        fen += " " + (this.activeColor == Color.WHITE ? "w" : "b");

        // Castling rights
        fen += " ";
        if (this.castlingRights[0]) {
            fen += "K";
        }
        if (this.castlingRights[1]) {
            fen += "Q";
        }
        if (this.castlingRights[2]) {
            fen += "k";
        }
        if (this.castlingRights[3]) {
            fen += "q";
        }

        if (!this.castlingRights[0] && !this.castlingRights[1] && !this.castlingRights[2] && !this.castlingRights[3]) {
            fen += "-";
        }

        // En passant
        fen += " ";
        if (this.enPassantSquare != -1) {
            int[] rankAndFile = BoardUtils.getRankAndFileFromSquareIndex(this.enPassantSquare);
            int file = rankAndFile[1];
            int rank = rankAndFile[0];
            fen += (char) ('a' + file) + String.valueOf(rank + 1);
        } else {
            fen += "-";
        }

        // Halfmove clock and fullmove number
        fen += " " + this.halfmoveClock + " " + this.fullmoveNumber;

        return fen;
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

}
