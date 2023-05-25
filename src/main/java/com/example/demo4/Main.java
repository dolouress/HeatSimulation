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
        Simulation sim = new Simulation(100, 2,800, 600);
        //jos koliko vrucina
        root.getChildren().add(sim);
        //event listener kad se sim update da se update i pixels atomi..
        primaryStage.show();
        //pitaj za input parameters da li preko konzole
        //1.3 sta znaci ono da se adjustuje
    }


    public static void main(String[] args) {
        launch(args);
    }
}
