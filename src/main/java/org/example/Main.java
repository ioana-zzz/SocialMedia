package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.service.Service;

import org.example.UI.IntroWindow;
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        IntroWindow introWindow = new IntroWindow(Service.getInstance());
        introWindow.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}