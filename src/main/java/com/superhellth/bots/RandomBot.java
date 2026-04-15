package com.superhellth.bots;

import java.util.List;

import com.superhellth.basics.Move;

public class RandomBot extends Bot {

    @Override
    public Move selectMove(List<Move> legalMoves) {
        // Implementation for selecting a random legal move
        int randInt = (int) (Math.random() * legalMoves.size());
        return legalMoves.get(randInt);
    }

    @Override
    public String getName() {
        return "Random Bot";
    }

    @Override
    public String getDescription() {
        return "A bot that selects a random legal move each turn.";
    }
}
