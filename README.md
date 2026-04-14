# Super Chess

A chess engine and interactive GUI built with Java and JavaFX.

<img width="300" height="300" alt="image" src="https://github.com/user-attachments/assets/b894b076-8b9f-4422-8dba-b1009aa0d771" />

## Features

- **Bitboard-based board representation** -- uses `long[2][6]` bitboards (2 colors x 6 piece types) with redundant square-lookup arrays for O(1) access
- **Magic bitboard move generation** -- sliding piece attacks (rook, bishop, queen) computed via magic bitboard lookups; knight and king use precomputed attack tables
- **Full legal move generation** -- two-phase approach: pseudo-legal generation with attack maps, then filtering by pin detection, king safety, and check evasion
- **Complete chess rules** -- castling, en passant, pawn promotion, check/checkmate detection
- **Interactive JavaFX GUI** -- click-to-move interface with legal move highlighting and promotion dialogs
- **Perft testing** -- node count verification for move generation correctness

## Requirements

- Java 17+
- Maven

## Build & Run

```bash
# Build
mvn clean install

# Run the GUI
mvn javafx:run

# Run tests
mvn test

# Run a single test
mvn test -Dtest=PerftTest
```

## Project Structure

```
src/main/java/com/superhellth/
  app/          App              -- entry point
  basics/       Board            -- bitboard representation, FEN support
                Game             -- game state, move execution
                Move             -- move representation
                MoveGenerator    -- pseudo-legal and legal move generation
                Color            -- color enum
                PieceType        -- piece type enum
                Direction        -- shift directions for bitboard ops
  ui/           MainWindow       -- main JavaFX window
                BoardGrid        -- 8x8 grid with click handling
                BoardSquare      -- individual square rendering
  utils/        BitboardUtils    -- bitboard shifts and flood fills
                BoardUtils       -- coordinate/rank/file conversions
                MagicConstants   -- magic numbers, masks, attack tables
```

## How It Works

### Board Representation

Each piece type for each color is stored as a 64-bit integer where each bit maps to a square (a1=0, h8=63). Occupancy bitboards track white, black, and empty squares. This allows efficient set operations (union, intersection, complement) for move generation.

### Move Generation

1. **Pseudo-legal phase**: generates all candidate moves per piece type and builds attack bitboards. Non-king moves are generated first so king moves can reference opponent attack maps.
2. **Legal filtering**: removes moves that leave the king in check by detecting absolute pins (ray from king through friendly piece to enemy slider) and validating king moves against opponent attacks. En passant includes special handling for horizontal pin detection.

### Magic Bitboards

Sliding pieces use precomputed magic bitboard tables for O(1) attack lookup. A blocker mask is combined with a magic number to index into a precomputed attack table, avoiding expensive ray traversal at runtime. All constants live in `MagicConstants`.
