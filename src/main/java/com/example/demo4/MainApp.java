package com.example.demo4;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Scanner;

public class MainApp extends Application {
    int width = 800, height = 600, heats = 10;
    int choice = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scanner scanner = new Scanner(System.in);
        /*System.out.println("Input width:");
        width = scanner.nextInt();
        System.out.println("Input height:");
        height = scanner.nextInt();
        System.out.println("Input number of hear sources:");
        heats = scanner.nextInt();*/


        System.out.println("Choose simulation mode:");
        System.out.println("1. Sequential");
        System.out.println("2. Parallel");

        choice = scanner.nextInt();

        if (choice == 1) {
            System.out.println("Starting Sequential Simulation...");
            SimulationSeq simulationSequential = new SimulationSeq(width, height, heats);
            simulationSequential.start(primaryStage);
        } else if (choice == 2) {
            System.out.println("Starting Parallel Simulation...");
            SimulationParallel simulationParallel = new SimulationParallel(width, height, heats);
            simulationParallel.start(primaryStage);
        } else {
            System.out.println("Invalid choice. Exiting...");
        }

        scanner.close();
    }
}
