package com.superhellth.bots;

import java.util.List;

import com.superhellth.basics.Board;
import com.superhellth.basics.Move;
import com.superhellth.basics.MoveGenerator;

public abstract class Bot {

    protected Board board;
    protected MoveGenerator moveGenerator;

    public void setup(Board board) {
        this.board = board;
        this.moveGenerator = new MoveGenerator(board);
    }

    public abstract Move selectMove(List<Move> legalMoves);

    public abstract String getName();

    public abstract String getDescription();

}
