package com.superhellth.app;

import com.superhellth.basics.Board;
import com.superhellth.ui.MainWindow;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Hello world!
 */
public class App extends Application {

    @Override   
      public void start(Stage stage) {
          Board board = new Board();
          MainWindow mainWindow = new MainWindow(board);
          mainWindow.show(stage);
      }

      public static void main(String[] args) {
          launch(args);
      }
}
