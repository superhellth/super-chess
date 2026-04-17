package com.superhellth.app;

import java.util.HashMap;
import java.util.Map;

import com.superhellth.basics.Color;
import com.superhellth.basics.Game;
import com.superhellth.basics.GameState;
import com.superhellth.basics.Move;
import com.superhellth.bots.Bot;
import com.superhellth.utils.BoardUtils;

class Simulation {

    private Game game;
    private Bot bot1;
    private Bot bot2;

    public Simulation(Bot bot1, Bot bot2) {
        this.game = new Game();
        this.bot1 = bot1;
        this.bot2 = bot2;
    }

    public void run(int numGames) {
        int draws = 0;
        int bot1Wins = 0;
        int bot2Wins = 0;

        for (int i = 0; i < numGames; i++) {
            if (i != 0 && i % 10 == 0) {
                System.out.println("Completed " + i + " games...");
                System.out.println("Draw rate: " + (draws / (double) (i + 1) * 100) + "%");
            }

            this.game = new Game(); // Reset game for each simulation
            this.bot1.setup(this.game);
            this.bot2.setup(this.game);
            Map<Color, Bot> botByColor = new HashMap<>();
            Color bot1Color = i % 2 == 0 ? Color.WHITE : Color.BLACK;
            Color bot2Color = BoardUtils.getOppositeColor(bot1Color);
            botByColor.put(bot1Color, this.bot1);
            botByColor.put(bot2Color, this.bot2);
            int moveCount = 0;
            while (game.getGameState() == GameState.ONGOING) {
                Bot currentBot = botByColor.get(game.getBoard().getActiveColor());
                moveCount++;
                Move move = currentBot.selectMove();
                game.makeMove(move);
            }

            GameState result = game.getGameState();
            if (result.getWinColor() == Color.EMPTY) {
                draws++;
            } else if (result.getWinColor() == bot1Color) {
                bot1Wins++;
            } else if (result.getWinColor() == bot2Color) {
                bot2Wins++;
            }
        }

        System.out.println();
        System.out.println("Simulation Results:");
        System.out.println(this.bot1.getName() + " Wins: " + bot1Wins);
        System.out.println(this.bot2.getName() + " Wins: " + bot2Wins);
        System.out.println("Draws: " + draws);
        System.out.println(this.bot1.getName() + " Avg ms per move: " + this.bot1.getAvgMsPerMove());
        System.out.println(this.bot2.getName() + " Avg ms per move: " + this.bot2.getAvgMsPerMove());
    }

}
