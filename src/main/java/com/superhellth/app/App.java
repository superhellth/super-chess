package com.superhellth.app;

import com.superhellth.basics.Game;
import com.superhellth.bots.Bot;
import com.superhellth.bots.RandomBot;
import com.superhellth.ui.MainWindow;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Hello world!
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Game game = new Game();
        MainWindow mainWindow = new MainWindow(game);
        mainWindow.show(stage);
    }

    public static void main(String[] args) {
        // launch(args); // for ui
        Bot whiteBot = new RandomBot();
        Bot blackBot = new RandomBot();
        Simulation simulation = new Simulation(whiteBot, blackBot);
        simulation.run(10000);
        System.exit(0);
    }
}
