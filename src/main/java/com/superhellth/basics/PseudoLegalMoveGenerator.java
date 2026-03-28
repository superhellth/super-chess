package com.superhellth.basics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.superhellth.utils.BitboardUtils;
import com.superhellth.utils.BoardUtils;
import com.superhellth.utils.MagicConstants;

public class PseudoLegalMoveGenerator {

    // Lookup tables
    public static final long[] KNIGHT_ATTACKS = new long[64];
    public static final long[] KING_ATTACKS = new long[64];

    // Logic
    private final Board board;
    private List<Move> allMoves;
    private Map<Color, List<Move>> movesByColor;

    // Attack bitboards: 0 white, 1 black
    private final Map<PieceType, Long[]> attackBitboards;

    public PseudoLegalMoveGenerator(Board board) {
        this.board = board;
        this.attackBitboards = new HashMap<>();
        for (PieceType type : PieceType.values()) {
            if (type == PieceType.EMPTY) {
                continue;
            }
            this.attackBitboards.put(type, new Long[]{0L, 0L});
        }

        PseudoLegalMoveGenerator.initializeKnightAttacks();
        PseudoLegalMoveGenerator.initializeKingAttacks();
    }

    public void resetAttackBitboards() {
        for (PieceType type : PieceType.values()) {
            if (type == PieceType.EMPTY) {
                continue;
            }
            this.attackBitboards.get(type)[0] = 0L;
            this.attackBitboards.get(type)[1] = 0L;
        }
    }

    public List<Move> generateAllMoves() {
        this.resetAttackBitboards();
        this.allMoves = new ArrayList<>();
        this.movesByColor = new HashMap<>();
        this.allMoves.addAll(this.generateAllMovesByColor(Color.WHITE));
        this.allMoves.addAll(this.generateAllMovesByColor(Color.BLACK));
        return this.allMoves;
    }

    public List<Move> generateAllMovesByColor(Color color) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when getting legal moves by color";
        this.movesByColor.put(color, new ArrayList<>());
        this.movesByColor.get(color).addAll(this.generatePawnMoves(color));
        this.movesByColor.get(color).addAll(this.generateKnightMoves(color));
        this.movesByColor.get(color).addAll(this.generateKingMoves(color));
        this.movesByColor.get(color).addAll(this.generateMovesFromMagic(color, PieceType.ROOK));
        this.movesByColor.get(color).addAll(this.generateMovesFromMagic(color, PieceType.BISHOP));
        this.movesByColor.get(color).addAll(this.generateMovesFromMagic(color, PieceType.QUEEN));
        return this.movesByColor.get(color);
    }

    // Getters for moves by square
    public List<Move> getAllMovesBySquareList(int squareIndex) {
        return this.allMoves.stream()
                .filter(move -> move.getFromSquare() == squareIndex)
                .toList();
    }

    public long getAllMovesBySquareBitboard(int squareIndex) {
        return this.allMoves.stream()
                .filter(move -> move.getFromSquare() == squareIndex)
                .reduce(0L, (bitboard, move) -> bitboard | (1L << move.getToSquare()), (a, b) -> a | b);
    }

    // Bitboard getters for UI visualization
    public long getAttackBitboard(PieceType type, Color color) {
        assert type != PieceType.EMPTY && color != Color.EMPTY : "Type and color cannot be EMPTY when getting attack bitboards";
        return this.attackBitboards.get(type)[color.ordinal()];
    }

    // Move generation
    private List<Move> generatePawnMoves(Color color) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when generating pawn moves";

        // Prepare bitboards
        long pawnBitboard = board.getPieceBitboard(color, PieceType.PAWN);
        long emptyBitboard = board.getOccupancyBitboard(Color.EMPTY);
        long oppositeColorBitboard = board.getOccupancyBitboard(BoardUtils.getOppositeColor(color));

        // Generate targets
        long singlePushTargets = generateSinglePushTargets(color, pawnBitboard, emptyBitboard);
        long doublePushTargets = generateDoublePushTargets(color, singlePushTargets, emptyBitboard);
        long eastAttacks = generatePawnAttacks(color, Direction.EAST, pawnBitboard, oppositeColorBitboard);
        long westAttacks = generatePawnAttacks(color, Direction.WEST, pawnBitboard, oppositeColorBitboard);
        this.attackBitboards.get(PieceType.PAWN)[color.ordinal()] = eastAttacks | westAttacks;

        // Extract moves from targets
        List<Move> moves = new ArrayList<>();
        moves.addAll(this.generateMovesFromBitboard(singlePushTargets, color == Color.WHITE ? 8 : -8));
        moves.addAll(this.generateMovesFromBitboard(doublePushTargets, color == Color.WHITE ? 16 : -16));
        moves.addAll(this.generateMovesFromBitboard(eastAttacks, color == Color.WHITE ? 9 : -7));
        moves.addAll(this.generateMovesFromBitboard(westAttacks, color == Color.WHITE ? 7 : -9));

        return moves;
    }

    private List<Move> generateKnightMoves(Color color) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when generating knight moves";
        long knightBitboard = this.board.getPieceBitboard(color, PieceType.KNIGHT);
        long occupancyBitboard = this.board.getOccupancyBitboard(color);

        List<Move> knightMoves = new ArrayList<>();
        long knightsToProcess = knightBitboard;
        while (knightsToProcess != 0L) {
            int fromSquare = Long.numberOfTrailingZeros(knightsToProcess);
            long validMoves = KNIGHT_ATTACKS[fromSquare] & ~occupancyBitboard;
            this.attackBitboards.get(PieceType.KNIGHT)[color.ordinal()] |= validMoves;

            while (validMoves != 0L) {
                int toSquare = Long.numberOfTrailingZeros(validMoves);
                knightMoves.add(new Move(fromSquare, toSquare));
                validMoves &= validMoves - 1L;
            }
            knightsToProcess &= knightsToProcess - 1L;
        }

        return knightMoves;
    }

    private List<Move> generateKingMoves(Color color) {
        long kingBitboard = this.board.getPieceBitboard(color, PieceType.KING);
        long occupancyBitboard = this.board.getOccupancyBitboard(color);

        List<Move> kingMoves = new ArrayList<>();
        int fromSquare = Long.numberOfTrailingZeros(kingBitboard);
        if (fromSquare < 64) {
            long validMoves = KING_ATTACKS[fromSquare] & ~occupancyBitboard;
            this.attackBitboards.get(PieceType.KING)[color.ordinal()] |= validMoves;
            while (validMoves != 0L) {
                int toSquare = Long.numberOfTrailingZeros(validMoves);
                kingMoves.add(new Move(fromSquare, toSquare));
                validMoves &= validMoves - 1L;
            }
        }
        return kingMoves;
    }

    private List<Move> generateMovesFromMagic(Color color, PieceType pieceType) {
        assert (pieceType == PieceType.ROOK) || (pieceType == PieceType.BISHOP) || (pieceType == PieceType.QUEEN) : "Can only use magic for rooks, bishops and queens!";
        long pieceBitboard = this.board.getPieceBitboard(color, pieceType);
        long bothColorsOccupancy = ~this.board.getOccupancyBitboard(Color.EMPTY);
        long sameColorOccupancy = this.board.getOccupancyBitboard(color);
        long oppositeColorBitboard = this.board.getOccupancyBitboard(BoardUtils.getOppositeColor(color));

        List<Move> moves = new ArrayList<>();
        while (pieceBitboard != 0) {
            int squareIndex = Long.numberOfTrailingZeros(pieceBitboard);
            long legalTargets;
            if (pieceType == PieceType.QUEEN) {
                long rookLikeTargets = this.generateAttacksFromMagic(squareIndex, bothColorsOccupancy, sameColorOccupancy, PieceType.ROOK);
                long bishopLikeTargets = this.generateAttacksFromMagic(squareIndex, bothColorsOccupancy, sameColorOccupancy, PieceType.BISHOP);
                legalTargets = rookLikeTargets | bishopLikeTargets;
            } else {
                legalTargets = this.generateAttacksFromMagic(squareIndex, bothColorsOccupancy, sameColorOccupancy, pieceType);
            }
            long attacks = legalTargets & oppositeColorBitboard;
            this.attackBitboards.get(pieceType)[color.ordinal()] |= attacks;
            for (int targetSquare : BitboardUtils.getPopulatedIndices(legalTargets)) {
                moves.add(new Move(squareIndex, targetSquare));
            }
            pieceBitboard &= pieceBitboard - 1;
        }

        return moves;
    }

    private long generateAttacksFromMagic(int sourceSquare, long bothColorsOccupancy, long sameColorOccupancy, PieceType typeLike) {
        assert (typeLike == PieceType.ROOK) || (typeLike == PieceType.BISHOP) : "Can only use magic for rooks and bishops!";
        long[] masks = typeLike == PieceType.ROOK ? MagicConstants.ROOK_MASKS : MagicConstants.BISHOP_MASKS;
        long[] magics = typeLike == PieceType.ROOK ? MagicConstants.ROOK_MAGICS : MagicConstants.BISHOP_MAGICS;
        int[] shifts = typeLike == PieceType.ROOK ? MagicConstants.ROOK_SHIFTS : MagicConstants.BISHOP_SHIFTS;
        long[][] lookup = typeLike == PieceType.ROOK ? MagicConstants.ROOK_TABLE : MagicConstants.BISHOP_TABLE;
        long occupancy = bothColorsOccupancy & masks[sourceSquare];
        int index = (int) ((occupancy * magics[sourceSquare]) >>> (64 - shifts[sourceSquare]));
        long range = lookup[sourceSquare][index];
        return range & ~sameColorOccupancy;
    }

    private long generatePawnAttacks(Color color, Direction direction, long pawnBitboard, long oppositeColorBitboard) {
        long attacks;
        if (color == Color.WHITE) {
            attacks = BitboardUtils.shift(pawnBitboard, direction == Direction.EAST ? Direction.NORTH_EAST : Direction.NORTH_WEST);
        } else {
            attacks = BitboardUtils.shift(pawnBitboard, direction == Direction.EAST ? Direction.SOUTH_EAST : Direction.SOUTH_WEST);
        }
        return attacks & oppositeColorBitboard;
    }

    private long generateSinglePushTargets(Color color, long pawnBitboard, long emptyBitboard) {
        if (color == Color.WHITE) {
            return BitboardUtils.shift(pawnBitboard, Direction.NORTH) & emptyBitboard;
        }
        return BitboardUtils.shift(pawnBitboard, Direction.SOUTH) & emptyBitboard;
    }

    private long generateDoublePushTargets(Color color, long singlePushTargets, long emptyBitboard) {
        if (color == Color.WHITE) {
            long rank4 = 0x00000000FF000000L;
            return BitboardUtils.shift(singlePushTargets, Direction.NORTH) & rank4 & emptyBitboard;
        }
        long rank5 = 0x000000FF00000000L;
        return BitboardUtils.shift(singlePushTargets, Direction.SOUTH) & rank5 & emptyBitboard;
    }

    private List<Move> generateMovesFromBitboard(long bitboard, int offset) {
        List<Move> moves = new ArrayList<>();
        while (bitboard != 0) {
            int toSquare = Long.numberOfTrailingZeros(bitboard);
            int fromSquare = toSquare - offset;
            moves.add(new Move(fromSquare, toSquare));
            bitboard &= bitboard - 1;
        }
        return moves;
    }

    private static void initializeKnightAttacks() {
        for (int square = 0; square < 64; square++) {
            long knightBitboard = 1L << square;
            long attacks = 0L;

            // Up-left, up-right (two ranks up, one file left/right)
            attacks |= (knightBitboard << 15) & 0x7F7F7F7F7F7F7F7FL;
            attacks |= (knightBitboard << 17) & 0xFEFEFEFEFEFEFEFEL;
            // Right-up, right-down (two files right, one rank up/down)
            attacks |= (knightBitboard << 10) & 0xFCFCFCFCFCFCFCFCL;
            attacks |= (knightBitboard >>> 6) & 0xFCFCFCFCFCFCFCFCL;
            // Left-up, left-down (two files left, one rank up/down)
            attacks |= (knightBitboard << 6) & 0x3F3F3F3F3F3F3F3FL;
            attacks |= (knightBitboard >>> 10) & 0x3F3F3F3F3F3F3F3FL;
            // Down-left, down-right (two ranks down, one file left/right)
            attacks |= (knightBitboard >>> 17) & 0x7F7F7F7F7F7F7F7FL;
            attacks |= (knightBitboard >>> 15) & 0xFEFEFEFEFEFEFEFEL;

            KNIGHT_ATTACKS[square] = attacks;
        }
    }

    private static void initializeKingAttacks() {
        for (int square = 0; square < 64; square++) {
            long kingBitboard = 1L << square;
            long attacks = 0L;

            // Vertical (north, south)
            attacks |= (kingBitboard << 8);
            attacks |= (kingBitboard >>> 8);
            // Horizontal (east, west)
            attacks |= (kingBitboard << 1) & 0xFEFEFEFEFEFEFEFEL;
            attacks |= (kingBitboard >>> 1) & 0x7F7F7F7F7F7F7F7FL;
            // Diagonals (NE, NW, SE, SW)
            attacks |= (kingBitboard << 9) & 0xFEFEFEFEFEFEFEFEL;
            attacks |= (kingBitboard << 7) & 0x7F7F7F7F7F7F7F7FL;
            attacks |= (kingBitboard >>> 7) & 0xFEFEFEFEFEFEFEFEL;
            attacks |= (kingBitboard >>> 9) & 0x7F7F7F7F7F7F7F7FL;

            KING_ATTACKS[square] = attacks;
        }
    }
}
