package com.superhellth.app;

import com.superhellth.basics.Color;
import com.superhellth.basics.Game;
import com.superhellth.basics.GameState;
import com.superhellth.basics.Move;
import com.superhellth.bots.Bot;

class Simulation {

    private Game game;
    private Bot whiteBot;
    private Bot blackBot;

    public Simulation(Bot whiteBot, Bot blackBot) {
        this.game = new Game();
        this.whiteBot = whiteBot;
        this.blackBot = blackBot;
    }

    public void run(int numGames) {
        int draws = 0;
        int whiteWins = 0;
        int blackWins = 0;

        for (int i = 0; i < numGames; i++) {
            this.game = new Game(); // Reset game for each simulation
            int moveCount = 0;
            while (game.getGameState() == GameState.ONGOING) {
                moveCount++;
                Bot currentBot = game.getBoard().getActiveColor() == Color.WHITE ? this.whiteBot : this.blackBot;
                Move move = currentBot.selectMove(this.game.getLegalMoves());
                game.makeMove(move);
            }

            GameState result = game.getGameState();
            if (result == GameState.WHITE_WINS) {
                whiteWins++;
            } else if (result == GameState.BLACK_WINS) {
                blackWins++;
            } else {
                draws++;
            }
        }
        System.out.println("Simulation Results:");
        System.out.println("White Wins: " + whiteWins);
        System.out.println("Black Wins: " + blackWins);
        System.out.println("Draws: " + draws);

    }

}
