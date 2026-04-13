package com.superhellth.basics;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class PerftTest {

    private long perft(Game game, int depth) {
        if (depth == 0) {
            return 1;
        }

        List<Move> moves = game.getMoveGenerator().generateAllLegalMoves();
        Color activeColor = game.getBoard().getActiveColor();
        long nodes = 0;

        for (Move move : moves) {
            if (game.getBoard().getSquareColor(move.getFromSquare()) != activeColor) {
                continue;
            }
            String fenBackup = game.getBoard().toFEN();
            game.executeMove(move);
            nodes += perft(game, depth - 1);
            game.getBoard().loadFromFEN(fenBackup);
            game.getMoveGenerator().generateAllLegalMoves();
        }

        return nodes;
    }

    /**
     * Divide: prints per-root-move node counts for debugging.
     * Run manually when a perft test fails to isolate the faulty move.
     */
    private void divide(Game game, int depth) {
        List<Move> moves = game.getMoveGenerator().generateAllLegalMoves();
        long total = 0;

        for (Move move : moves) {
            String fenBackup = game.getBoard().toFEN();
            game.executeMove(move);
            long nodes = perft(game, depth - 1);
            total += nodes;
            String from = squareName(move.getFromSquare());
            String to = squareName(move.getToSquare());
            String promo = move.getPromotionPieceType() != PieceType.EMPTY
                    ? move.getPromotionPieceType().name().toLowerCase() : "";
            System.out.println(from + to + promo + ": " + nodes);
            game.getBoard().loadFromFEN(fenBackup);
            game.getMoveGenerator().generateAllLegalMoves();
        }

        System.out.println("Total: " + total);
    }

    private String squareName(int square) {
        int file = square % 8;
        int rank = square / 8;
        return "" + (char) ('a' + file) + (rank + 1);
    }

    // Starting position
    @ParameterizedTest
    @CsvSource({
            "1, 20",
            "2, 400",
            "3, 8902",
            "4, 197281",
            "5, 4865609"
    })
    void startingPosition(int depth, long expected) {
        Game game = new Game();
        assertEquals(expected, perft(game, depth));
    }

    // Kiwipete
    @ParameterizedTest
    @CsvSource({
            "1, 48",
            "2, 2039",
            "3, 97862",
            "4, 4085603"
    })
    void kiwipete(int depth, long expected) {
        Game game = new Game("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        assertEquals(expected, perft(game, depth));
    }

    // Position 3
    @ParameterizedTest
    @CsvSource({
            "1, 14",
            "2, 191",
            "3, 2812",
            "4, 43238",
            "5, 674624"
    })
    void position3(int depth, long expected) {
        Game game = new Game("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1");
        assertEquals(expected, perft(game, depth));
    }

    // Position 4
    @ParameterizedTest
    @CsvSource({
            "1, 6",
            "2, 264",
            "3, 9467",
            "4, 422333"
    })
    void position4(int depth, long expected) {
        Game game = new Game("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
        assertEquals(expected, perft(game, depth));
    }

    // Position 5
    @ParameterizedTest
    @CsvSource({
            "1, 44",
            "2, 1486",
            "3, 62379",
            "4, 2103487"
    })
    void position5(int depth, long expected) {
        Game game = new Game("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");
        assertEquals(expected, perft(game, depth));
    }
}
