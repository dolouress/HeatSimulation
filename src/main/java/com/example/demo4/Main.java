package com.example.demo4;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    public int WINDOW_WIDTH=800; // Desired window width
    public int WINDOW_HEIGHT = 800; // Desired window height

    public int NUM_OF_HEAT = 1000; // Desired window height



    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simulation");


        // Create the root container
        StackPane root = new StackPane();
        Simulation simulation = new Simulation(NUM_OF_HEAT, WINDOW_WIDTH, WINDOW_HEIGHT); // Pass the desired width and height to the Simulation constructor
        root.getChildren().add(simulation);

        // Create the scene with the root container
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);

        // Update the width and height properties when the window is resized
        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = newValue.doubleValue();
            simulation.setWidth1(newWidth);
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            double newHeight = newValue.doubleValue();
            simulation.setHeight1(newHeight);
        });

        primaryStage.setResizable(true); // Allow manual window resizing

        primaryStage.show();
    }
    public void setWINDOW_WIDTH(int WINDOW_WIDTH) {
        this.WINDOW_WIDTH = WINDOW_WIDTH;
    }

    public void main(String[] args) {
        launch(args);
    }
}
