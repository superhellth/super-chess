package com.superhellth.app;

import com.superhellth.ui.MainWindow;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Hello world!
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        MainWindow mainWindow = new MainWindow();
        mainWindow.show(stage);
    }

    public static void main(String[] args) {
        launch(args); // for ui
        // Bot bot1 = new EvalBot1(0);
        // Bot bot2 = new EvalBot1(2);
        // Simulation simulation = new Simulation(bot1, bot2);
        // simulation.run(20);
        // System.exit(0);
    }
}
