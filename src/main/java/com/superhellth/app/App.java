package com.superhellth.app;

import com.superhellth.basics.Game;
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
          launch(args);
      }
}
