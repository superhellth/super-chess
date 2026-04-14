package com.superhellth.basics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.superhellth.utils.BitboardUtils;
import com.superhellth.utils.BoardUtils;
import com.superhellth.utils.MagicConstants;

public class MoveGenerator {

    // Lookup tables
    public static final long[] KNIGHT_ATTACKS = new long[64];
    public static final long[] KING_ATTACKS = new long[64];

    // Logic
    private final Board board;
    private List<Move> allPseudoLegalMoves;
    private List<Move> allLegalMoves;

    // Attack bitboards: 0 white, 1 black
    private final Map<PieceType, Long[]> attackBitboards;

    public MoveGenerator(Board board) {
        this.board = board;
        this.attackBitboards = new HashMap<>();
        for (PieceType type : PieceType.values()) {
            if (type == PieceType.EMPTY) {
                continue;
            }
            this.attackBitboards.put(type, new Long[]{0L, 0L});
        }

        MoveGenerator.initializeKnightAttacks();
        MoveGenerator.initializeKingAttacks();
    }

    private void resetAttackBitboards() {
        for (PieceType type : PieceType.values()) {
            if (type == PieceType.EMPTY) {
                continue;
            }
            this.attackBitboards.get(type)[0] = 0L;
            this.attackBitboards.get(type)[1] = 0L;
        }
    }

    // Opposite direction indices: N↔S, E↔W, NW↔SE, NE↔SW
    private static final int[] OPPOSITE_DIR = {1, 0, 3, 2, 7, 6, 5, 4};

    // Whether the closest blocker on this ray has the lowest bit index (true) or highest (false)
    private static final boolean[] RAY_SCAN_LSB = {
        true,   // NORTH
        false,  // SOUTH
        true,   // EAST
        false,  // WEST
        true,   // NORTH_WEST
        true,   // NORTH_EAST
        false,  // SOUTH_WEST
        false   // SOUTH_EAST
    };

    // Whether this ray direction is cardinal (rook/queen) or diagonal (bishop/queen)
    private static final boolean[] RAY_IS_CARDINAL = {
        true, true, true, true,     // N, S, E, W
        false, false, false, false  // NW, NE, SW, SE
    };

    public List<Move> generateAllPseudoLegalMoves() {
        this.resetAttackBitboards();
        this.allPseudoLegalMoves = new ArrayList<>();

        Color activeColor = board.getActiveColor();
        Color opponentColor = BoardUtils.getOppositeColor(activeColor);

        // Generate attack bitboards for opponent (needed for king move safety)
        this.generateAttacksOnly(opponentColor);

        // Generate moves + attack bitboards for active color (non-king first)
        this.generateNonKingMoves(activeColor);
        // King moves use opponent attack maps for safety checks
        this.allPseudoLegalMoves.addAll(this.generateKingMoves(activeColor));

        return this.allPseudoLegalMoves;
    }

    /**
     * Populates attack bitboards for the given color without generating move lists.
     * Used for opponent attacks needed by king move generation.
     */
    private void generateAttacksOnly(Color color) {
        // Pawns
        long pawnBitboard = board.getPieceBitboard(color, PieceType.PAWN);
        long eastAttacks = generatePawnAttacks(color, Direction.EAST, pawnBitboard);
        long westAttacks = generatePawnAttacks(color, Direction.WEST, pawnBitboard);
        this.attackBitboards.get(PieceType.PAWN)[color.ordinal()] = eastAttacks | westAttacks;

        // Knights
        long knightBitboard = board.getPieceBitboard(color, PieceType.KNIGHT);
        while (knightBitboard != 0L) {
            int sq = Long.numberOfTrailingZeros(knightBitboard);
            this.attackBitboards.get(PieceType.KNIGHT)[color.ordinal()] |= KNIGHT_ATTACKS[sq];
            knightBitboard &= knightBitboard - 1L;
        }

        // Sliding pieces
        long bothOccupancy = ~board.getOccupancyBitboard(Color.EMPTY);
        for (PieceType type : new PieceType[]{PieceType.ROOK, PieceType.BISHOP, PieceType.QUEEN}) {
            long pieceBB = board.getPieceBitboard(color, type);
            while (pieceBB != 0) {
                int sq = Long.numberOfTrailingZeros(pieceBB);
                long attacks;
                if (type == PieceType.QUEEN) {
                    attacks = generateAttacksFromMagic(sq, bothOccupancy, PieceType.ROOK)
                            | generateAttacksFromMagic(sq, bothOccupancy, PieceType.BISHOP);
                } else {
                    attacks = generateAttacksFromMagic(sq, bothOccupancy, type);
                }
                this.attackBitboards.get(type)[color.ordinal()] |= attacks;
                pieceBB &= pieceBB - 1;
            }
        }

        // King
        long kingBB = board.getPieceBitboard(color, PieceType.KING);
        if (kingBB != 0) {
            int kingSq = Long.numberOfTrailingZeros(kingBB);
            this.attackBitboards.get(PieceType.KING)[color.ordinal()] |= KING_ATTACKS[kingSq];
        }
    }

    public List<Move> generateAllLegalMoves() {
        this.generateAllPseudoLegalMoves();
        this.allLegalMoves = new ArrayList<>();

        Color color = board.getActiveColor();
        Map<Integer, Long> pinMasks = computePinMasks(color);
        long checkMask = computeCheckMask(color);

        int kingSquare = Long.numberOfTrailingZeros(board.getPieceBitboard(color, PieceType.KING));
        Color opponentColor = BoardUtils.getOppositeColor(color);
        long opponentRooksQueens = board.getPieceBitboard(opponentColor, PieceType.ROOK)
                | board.getPieceBitboard(opponentColor, PieceType.QUEEN);

        for (Move move : this.allPseudoLegalMoves) {
            boolean isKingMove = board.getSquarePieceType(move.getFromSquare()) == PieceType.KING;

            // Check evasion: non-king moves must block or capture the checker
            if (!isKingMove) {
                // En passant can capture a checking pawn (target square != captured pawn square)
                boolean isEnPassantCapturingChecker = false;
                if (board.getEnPassantSquare() != -1
                        && board.getEnPassantSquare() == move.getToSquare()
                        && board.getSquarePieceType(move.getFromSquare()) == PieceType.PAWN) {
                    int capturedPawnSquare = color == Color.WHITE
                            ? move.getToSquare() - 8
                            : move.getToSquare() + 8;
                    isEnPassantCapturingChecker = (checkMask & (1L << capturedPawnSquare)) != 0;
                }

                if (!isEnPassantCapturingChecker && (checkMask & (1L << move.getToSquare())) == 0) {
                    continue;
                }
            }

            // Pin restriction: pinned pieces can only move along the pin ray
            Long pinRay = pinMasks.get(move.getFromSquare());
            if (pinRay != null && (pinRay & (1L << move.getToSquare())) == 0) {
                continue;
            }

            // En passant horizontal pin: removing both pawns may expose king to rook/queen
            if (board.getEnPassantSquare() != -1
                    && board.getEnPassantSquare() == move.getToSquare()
                    && board.getSquarePieceType(move.getFromSquare()) == PieceType.PAWN) {
                int capturedPawnSquare = color == Color.WHITE
                        ? move.getToSquare() - 8
                        : move.getToSquare() + 8;
                long occupancyAfter = (~board.getOccupancyBitboard(Color.EMPTY))
                        & ~(1L << move.getFromSquare())
                        & ~(1L << capturedPawnSquare)
                        | (1L << move.getToSquare());
                long kingRookAttacks = generateAttacksFromMagic(kingSquare, occupancyAfter, PieceType.ROOK);
                if ((kingRookAttacks & opponentRooksQueens) != 0) {
                    continue;
                }
            }

            this.allLegalMoves.add(move);
        }

        return this.allLegalMoves;
    }

    private Map<Integer, Long> computePinMasks(Color color) {
        Map<Integer, Long> pinMasks = new HashMap<>();

        int kingSquare = Long.numberOfTrailingZeros(board.getPieceBitboard(color, PieceType.KING));
        if (kingSquare >= 64) return pinMasks;

        long friendlyOccupancy = board.getOccupancyBitboard(color);
        Color opponentColor = BoardUtils.getOppositeColor(color);
        long opponentRooksQueens = board.getPieceBitboard(opponentColor, PieceType.ROOK)
                | board.getPieceBitboard(opponentColor, PieceType.QUEEN);
        long opponentBishopsQueens = board.getPieceBitboard(opponentColor, PieceType.BISHOP)
                | board.getPieceBitboard(opponentColor, PieceType.QUEEN);
        long allOccupancy = ~board.getOccupancyBitboard(Color.EMPTY);

        for (int dir = 0; dir < 8; dir++) {
            long ray = MagicConstants.RAY_MASKS[kingSquare][dir];
            long blockersOnRay = ray & allOccupancy;
            if (blockersOnRay == 0) continue;

            // Find first blocker (closest to king)
            int firstBlocker = RAY_SCAN_LSB[dir]
                    ? Long.numberOfTrailingZeros(blockersOnRay)
                    : 63 - Long.numberOfLeadingZeros(blockersOnRay);

            // First blocker must be a friendly piece
            if ((friendlyOccupancy & (1L << firstBlocker)) == 0) continue;

            // Find second blocker (the potential pinner)
            long remainingBlockers = blockersOnRay & ~(1L << firstBlocker);
            if (remainingBlockers == 0) continue;

            int secondBlocker = RAY_SCAN_LSB[dir]
                    ? Long.numberOfTrailingZeros(remainingBlockers)
                    : 63 - Long.numberOfLeadingZeros(remainingBlockers);

            // Second blocker must be an enemy slider matching this ray direction
            long relevantAttackers = RAY_IS_CARDINAL[dir] ? opponentRooksQueens : opponentBishopsQueens;
            if ((relevantAttackers & (1L << secondBlocker)) == 0) continue;

            // Pin detected: allowed squares are the ray from king up to and including pinner
            long pinRay = ray & ~MagicConstants.RAY_MASKS[secondBlocker][dir];
            pinMasks.put(firstBlocker, pinRay);
        }

        return pinMasks;
    }

    /**
     * Computes a bitmask of squares that non-king pieces are allowed to move to.
     * - No check: ~0L (all squares allowed)
     * - Single check: checker square + interposing squares (for sliding checkers)
     * - Double check: 0L (only king moves are legal)
     */
    private long computeCheckMask(Color color) {
        int kingSquare = Long.numberOfTrailingZeros(board.getPieceBitboard(color, PieceType.KING));
        if (kingSquare >= 64) return ~0L;

        Color opponentColor = BoardUtils.getOppositeColor(color);
        long allOccupancy = ~board.getOccupancyBitboard(Color.EMPTY);
        long checkers = 0L;

        // Knight checks
        checkers |= KNIGHT_ATTACKS[kingSquare] & board.getPieceBitboard(opponentColor, PieceType.KNIGHT);

        // Pawn checks (attack from king's perspective as if king were a pawn)
        long kingBit = 1L << kingSquare;
        long pawnAttacksFromKing = generatePawnAttacks(color, Direction.EAST, kingBit)
                | generatePawnAttacks(color, Direction.WEST, kingBit);
        checkers |= pawnAttacksFromKing & board.getPieceBitboard(opponentColor, PieceType.PAWN);

        // Sliding piece checks
        long rookAttacksFromKing = generateAttacksFromMagic(kingSquare, allOccupancy, PieceType.ROOK);
        long bishopAttacksFromKing = generateAttacksFromMagic(kingSquare, allOccupancy, PieceType.BISHOP);
        checkers |= rookAttacksFromKing & (board.getPieceBitboard(opponentColor, PieceType.ROOK)
                | board.getPieceBitboard(opponentColor, PieceType.QUEEN));
        checkers |= bishopAttacksFromKing & (board.getPieceBitboard(opponentColor, PieceType.BISHOP)
                | board.getPieceBitboard(opponentColor, PieceType.QUEEN));

        int checkerCount = Long.bitCount(checkers);
        if (checkerCount == 0) return ~0L;
        if (checkerCount >= 2) return 0L;

        // Single check: mask = checker square + interposing squares for sliding checkers
        int checkerSquare = Long.numberOfTrailingZeros(checkers);
        long checkMask = 1L << checkerSquare;

        PieceType checkerType = board.getSquarePieceType(checkerSquare);
        if (checkerType == PieceType.ROOK || checkerType == PieceType.BISHOP || checkerType == PieceType.QUEEN) {
            // Find the ray direction from king to checker
            for (int dir = 0; dir < 8; dir++) {
                long ray = MagicConstants.RAY_MASKS[kingSquare][dir];
                if ((ray & (1L << checkerSquare)) != 0) {
                    // Interposing squares: on the ray from king, before the checker
                    long interposing = ray & ~MagicConstants.RAY_MASKS[checkerSquare][dir] & ~(1L << checkerSquare);
                    checkMask |= interposing;
                    break;
                }
            }
        }

        return checkMask;
    }

    // Getters for moves by square
    public List<Move> getAllMovesBySquareList(int squareIndex) {
        return this.allLegalMoves.stream()
                .filter(move -> move.getFromSquare() == squareIndex)
                .toList();
    }

    public long getAllMovesBySquareBitboard(int squareIndex) {
        return this.allLegalMoves.stream()
                .filter(move -> move.getFromSquare() == squareIndex)
                .reduce(0L, (bitboard, move) -> bitboard | (1L << move.getToSquare()), (a, b) -> a | b);
    }

    // Bitboard getters for UI visualization
    public long getAttackBitboard(PieceType type, Color color) {
        assert type != PieceType.EMPTY && color != Color.EMPTY : "Type and color cannot be EMPTY when getting attack bitboards";
        return this.attackBitboards.get(type)[color.ordinal()];
    }

    // Move generation
    private void generateNonKingMoves(Color color) {
        this.allPseudoLegalMoves.addAll(this.generatePawnMoves(color));
        this.allPseudoLegalMoves.addAll(this.generateKnightMoves(color));
        this.allPseudoLegalMoves.addAll(this.generateMovesFromMagic(color, PieceType.ROOK));
        this.allPseudoLegalMoves.addAll(this.generateMovesFromMagic(color, PieceType.BISHOP));
        this.allPseudoLegalMoves.addAll(this.generateMovesFromMagic(color, PieceType.QUEEN));
    }

    private List<Move> generatePawnMoves(Color color) {
        assert color != Color.EMPTY : "Color cannot be EMPTY when generating pawn moves";

        // Prepare bitboards
        long pawnBitboard = board.getPieceBitboard(color, PieceType.PAWN);
        long emptyBitboard = board.getOccupancyBitboard(Color.EMPTY);
        long oppositeColorBitboard = board.getOccupancyBitboard(BoardUtils.getOppositeColor(color));

        // Generate targets
        long singlePushTargets = generateSinglePushTargets(color, pawnBitboard, emptyBitboard);
        long doublePushTargets = generateDoublePushTargets(color, singlePushTargets, emptyBitboard);
        long eastAttacks = generatePawnAttacks(color, Direction.EAST, pawnBitboard);
        long westAttacks = generatePawnAttacks(color, Direction.WEST, pawnBitboard);
        this.attackBitboards.get(PieceType.PAWN)[color.ordinal()] = eastAttacks | westAttacks;
        long oppositeColorMask = oppositeColorBitboard | (board.getEnPassantSquare() != -1 ? (1L << board.getEnPassantSquare()) : 0L);
        eastAttacks &= oppositeColorMask;
        westAttacks &= oppositeColorMask;

        // Extract moves from targets
        List<Move> moves = new ArrayList<>();
        moves.addAll(this.generatePawnMovesFromBitboard(singlePushTargets, color == Color.WHITE ? 8 : -8));
        moves.addAll(this.generatePawnMovesFromBitboard(doublePushTargets, color == Color.WHITE ? 16 : -16));
        moves.addAll(this.generatePawnMovesFromBitboard(eastAttacks, color == Color.WHITE ? 9 : -7));
        moves.addAll(this.generatePawnMovesFromBitboard(westAttacks, color == Color.WHITE ? 7 : -9));

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
            long knightAttacks = KNIGHT_ATTACKS[fromSquare];
            this.attackBitboards.get(PieceType.KNIGHT)[color.ordinal()] |= knightAttacks;
            knightAttacks &= ~occupancyBitboard;

            while (knightAttacks != 0L) {
                int toSquare = Long.numberOfTrailingZeros(knightAttacks);
                knightMoves.add(new Move(fromSquare, toSquare));
                knightAttacks &= knightAttacks - 1L;
            }
            knightsToProcess &= knightsToProcess - 1L;
        }

        return knightMoves;
    }

    // Castling constants: [white kingside, white queenside, black kingside, black queenside]
    private static final int[] KING_HOME = {4, 4, 60, 60};
    private static final int[] KING_TARGET = {6, 2, 62, 58};
    private static final long[] EMPTY_MASKS = {0x60L, 0x0EL, 0x60L << 56, 0x0EL << 56};
    private static final long[] SAFE_MASKS = {0x70L, 0x1CL, 0x70L << 56, 0x1CL << 56};

    private long computeSlidingAttacksWithoutKing(Color opponentColor, long occupancyWithoutKing) {
        long slidingAttacks = 0L;
        long rooks = board.getPieceBitboard(opponentColor, PieceType.ROOK);
        long bishops = board.getPieceBitboard(opponentColor, PieceType.BISHOP);
        long queens = board.getPieceBitboard(opponentColor, PieceType.QUEEN);

        long rooksAndQueens = rooks | queens;
        while (rooksAndQueens != 0) {
            int sq = Long.numberOfTrailingZeros(rooksAndQueens);
            slidingAttacks |= generateAttacksFromMagic(sq, occupancyWithoutKing, PieceType.ROOK);
            rooksAndQueens &= rooksAndQueens - 1;
        }
        long bishopsAndQueens = bishops | queens;
        while (bishopsAndQueens != 0) {
            int sq = Long.numberOfTrailingZeros(bishopsAndQueens);
            slidingAttacks |= generateAttacksFromMagic(sq, occupancyWithoutKing, PieceType.BISHOP);
            bishopsAndQueens &= bishopsAndQueens - 1;
        }
        return slidingAttacks;
    }

    private List<Move> generateKingMoves(Color color) {
        long kingBitboard = this.board.getPieceBitboard(color, PieceType.KING);
        long sameColorOccupancy = this.board.getOccupancyBitboard(color);
        Color opponentColor = BoardUtils.getOppositeColor(color);

        // Recompute sliding attacks with king removed to prevent x-ray hiding
        long occupancyWithoutKing = (~board.getOccupancyBitboard(Color.EMPTY)) & ~kingBitboard;
        long opponentSlidingAttacks = computeSlidingAttacksWithoutKing(opponentColor, occupancyWithoutKing);
        long opponentNonSlidingAttacks = this.attackBitboards.get(PieceType.PAWN)[opponentColor.ordinal()]
                | this.attackBitboards.get(PieceType.KNIGHT)[opponentColor.ordinal()]
                | this.attackBitboards.get(PieceType.KING)[opponentColor.ordinal()];
        long opponentAttacks = opponentSlidingAttacks | opponentNonSlidingAttacks;

        List<Move> kingMoves = new ArrayList<>();
        int fromSquare = Long.numberOfTrailingZeros(kingBitboard);
        if (fromSquare >= 64) {
            return kingMoves;
        }
        long kingAttacks = KING_ATTACKS[fromSquare];
        this.attackBitboards.get(PieceType.KING)[color.ordinal()] |= kingAttacks;
        kingAttacks &= ~sameColorOccupancy;
        kingAttacks &= ~opponentAttacks;
        while (kingAttacks != 0L) {
            int toSquare = Long.numberOfTrailingZeros(kingAttacks);
            kingMoves.add(new Move(fromSquare, toSquare));
            kingAttacks &= kingAttacks - 1L;
        }

        // Castling
        boolean[] castlingRights = this.board.getCastlingRights(color);
        long allOccupancy = ~this.board.getOccupancyBitboard(Color.EMPTY);
        int base = color == Color.WHITE ? 0 : 2;

        for (int side = 0; side < 2; side++) {
            int i = base + side;
            if (!castlingRights[side]) {
                continue;
            }
            if (fromSquare != KING_HOME[i]) {
                continue;
            }
            if ((allOccupancy & EMPTY_MASKS[i]) != 0) {
                continue;
            }
            if ((opponentAttacks & SAFE_MASKS[i]) != 0) {
                continue;
            }
            kingMoves.add(new Move(fromSquare, KING_TARGET[i]));
        }

        return kingMoves;
    }

    private List<Move> generateMovesFromMagic(Color color, PieceType pieceType) {
        assert (pieceType == PieceType.ROOK) || (pieceType == PieceType.BISHOP) || (pieceType == PieceType.QUEEN) : "Can only use magic for rooks, bishops and queens!";
        long pieceBitboard = this.board.getPieceBitboard(color, pieceType);
        long bothColorsOccupancy = ~this.board.getOccupancyBitboard(Color.EMPTY);
        long sameColorOccupancy = this.board.getOccupancyBitboard(color);
        // long oppositeColorBitboard = this.board.getOccupancyBitboard(BoardUtils.getOppositeColor(color));

        List<Move> moves = new ArrayList<>();
        while (pieceBitboard != 0) {
            int squareIndex = Long.numberOfTrailingZeros(pieceBitboard);
            long allTargets;
            if (pieceType == PieceType.QUEEN) {
                long rookLikeTargets = this.generateAttacksFromMagic(squareIndex, bothColorsOccupancy, PieceType.ROOK);
                long bishopLikeTargets = this.generateAttacksFromMagic(squareIndex, bothColorsOccupancy, PieceType.BISHOP);
                allTargets = rookLikeTargets | bishopLikeTargets;
            } else {
                allTargets = this.generateAttacksFromMagic(squareIndex, bothColorsOccupancy, pieceType);
            }
            this.attackBitboards.get(pieceType)[color.ordinal()] |= allTargets;
            allTargets &= ~sameColorOccupancy;
            for (int targetSquare : BitboardUtils.getPopulatedIndices(allTargets)) {
                moves.add(new Move(squareIndex, targetSquare));
            }
            pieceBitboard &= pieceBitboard - 1;
        }

        return moves;
    }

    private long generateAttacksFromMagic(int sourceSquare, long bothColorsOccupancy, PieceType typeLike) {
        assert (typeLike == PieceType.ROOK) || (typeLike == PieceType.BISHOP) : "Can only use magic for rooks and bishops!";
        long[] masks = typeLike == PieceType.ROOK ? MagicConstants.ROOK_MASKS : MagicConstants.BISHOP_MASKS;
        long[] magics = typeLike == PieceType.ROOK ? MagicConstants.ROOK_MAGICS : MagicConstants.BISHOP_MAGICS;
        int[] shifts = typeLike == PieceType.ROOK ? MagicConstants.ROOK_SHIFTS : MagicConstants.BISHOP_SHIFTS;
        long[][] lookup = typeLike == PieceType.ROOK ? MagicConstants.ROOK_TABLE : MagicConstants.BISHOP_TABLE;
        long occupancy = bothColorsOccupancy & masks[sourceSquare];
        int index = (int) ((occupancy * magics[sourceSquare]) >>> (64 - shifts[sourceSquare]));
        long range = lookup[sourceSquare][index];
        return range;
    }

    private long generatePawnAttacks(Color color, Direction direction, long pawnBitboard) {
        long attacks;
        if (color == Color.WHITE) {
            attacks = BitboardUtils.shift(pawnBitboard, direction == Direction.EAST ? Direction.NORTH_EAST : Direction.NORTH_WEST);
        } else {
            attacks = BitboardUtils.shift(pawnBitboard, direction == Direction.EAST ? Direction.SOUTH_EAST : Direction.SOUTH_WEST);
        }
        return attacks;
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

    private final static int[][] PAWN_PROMOTION_SQUARES = {
        {56, 57, 58, 59, 60, 61, 62, 63},
        {0, 1, 2, 3, 4, 5, 6, 7}
    };

    private List<Move> generatePawnMovesFromBitboard(long bitboard, int offset) {
        List<Move> moves = new ArrayList<>();
        while (bitboard != 0) {
            int toSquare = Long.numberOfTrailingZeros(bitboard);
            int fromSquare = toSquare - offset;
            int[] promotionRanks = offset > 0 ? PAWN_PROMOTION_SQUARES[0] : PAWN_PROMOTION_SQUARES[1];
            if (IntStream.of(promotionRanks).anyMatch(rank -> rank == toSquare)) {
                for (PieceType promotionPiece : new PieceType[]{PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT}) {
                    Move promotionMove = new Move(fromSquare, toSquare);
                    promotionMove.setPromotionPieceType(promotionPiece);
                    moves.add(promotionMove);
                }
            } else {
                moves.add(new Move(fromSquare, toSquare));
            }
            bitboard &= bitboard - 1;
        }
        return moves;
    }

    private long getColorAttacks(Color color) {
        long attacks = 0L;
        for (PieceType type : PieceType.values()) {
            if (type == PieceType.EMPTY) {
                continue;
            }
            attacks |= this.attackBitboards.get(type)[color.ordinal()];
        }
        return attacks;
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
