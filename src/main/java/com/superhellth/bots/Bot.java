package com.superhellth.bots;

import com.superhellth.basics.Game;
import com.superhellth.basics.Move;
import com.superhellth.basics.MoveGenerator;

public abstract class Bot {

    protected Game game;
    protected MoveGenerator moveGenerator;
    protected int totalMovesMade = 0;
    protected int totalTimeUsed = 0;

    public void setup(Game game) {
        this.game = game;
        this.moveGenerator = game.getMoveGenerator();
    }

    public Move selectMove() {
        long startTime = System.currentTimeMillis();
        Move move = this.selectMoveImpl();
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);

        this.totalTimeUsed += duration;
        this.totalMovesMade++;

        return move;
    }

    public abstract Move selectMoveImpl();

    public abstract String getName();

    public abstract String getDescription();

    public long getAvgMsPerMove() {
        if (this.totalMovesMade == 0) {
            return 0;
        }
        return this.totalTimeUsed / this.totalMovesMade;
    }

}
