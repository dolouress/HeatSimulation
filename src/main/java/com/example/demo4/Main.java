package com.example.demo4;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Group root = new Group();
        primaryStage.setTitle("Heat Simulation");
        primaryStage.setScene(new Scene(root));
        Simulation sim = new Simulation(100, 50,800, 600);
        root.getChildren().add(sim);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
