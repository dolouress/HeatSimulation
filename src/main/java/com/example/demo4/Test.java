package com.example.demo4;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;
import java.util.concurrent.*;

public class Test extends Application{
    private static final long SEED = 1234; // Seed value for the random generator
    private static final int WINDOW_WIDTH = 800; // Desired window width
    private static final int WINDOW_HEIGHT = 600; // Desired window height
    private static final int NUM_OF_HEAT = 10; // Number of heat sources
    private static final int NUM_THREADS = 10; // Number of parallel threads
    private static Random rand = new Random(SEED);
    private static int[] arrayOfRandoms;

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
    public static void main(String[] args) {
        launch(args);
    }

    public static class Simulation extends Canvas {
        public static int n, numOfHeat, size;
        public static Atom[][] grid;
        public static GraphicsContext gc;
        public boolean finished = false;
        public static boolean stable = false;
        private int index;
        public static boolean firstIteration = true;

        public Simulation(int numOfHeat, double width, double height) {
            super(width, height);
            this.n = calculateNumOfAtoms(width, height);
            this.size = calculateAtomSize(width, height, this.n);
            this.gc = getGraphicsContext2D();
            this.grid = initializeGrid(n);
            this.numOfHeat = numOfHeat;
            this.firstIteration = true;
            this.setWidth(width);
            this.setHeight(height);

            // Random points for heat sources
            if (arrayOfRandoms == null) {
                arrayOfRandoms = new int[2 * numOfHeat];
                for (int i = 0; i < numOfHeat * 2; i++) {
                    int broj = rand.nextInt(n - 1);
                    arrayOfRandoms[i] = broj;
                    System.out.println(broj);
                }
            }

            ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
            CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);

            new AnimationTimer() {
                private long startTime = System.nanoTime();

                @Override
                public void handle(long now) {
                    if (finished) return;
                    if (!stable) {
                        paint();
                        fixed();
                        firstIteration = false;
                        for (int i = 0; i < NUM_THREADS; i++) {
                            int start = i * (n / NUM_THREADS);
                            int end = start + (n / NUM_THREADS);
                            executorService.execute(new SimulationTask(start, end, barrier));
                        }
                    } else {
                        long endTime = System.nanoTime();
                        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                        System.out.println("Simulation completed in " + duration + "ms");
                        finished = true;
                        executorService.shutdown();
                        try {
                            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                                executorService.shutdownNow();
                            }
                        } catch (InterruptedException e) {
                            executorService.shutdownNow();
                        }
                    }
                }
            }.start();
        }
        public void fixed(){
            index = 0;
            for (int k = 0; k < numOfHeat; k++) {
                SimulationTask.applyFixedTemperature(arrayOfRandoms[index], arrayOfRandoms[index + 1], 100);
                index += 2;
            }
        }

        public void paint() {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    Atom currentAtom = grid[i][j];
                    Color newColor;
                    double newTemperature = 0;
                    if (!firstIteration) {
                        newTemperature = SimulationTask.calculateNewTemperature(i, j);
                        newColor = currentAtom.getTemperatureColor(newTemperature);
                        if (newColor != Color.BLUE) {
                            currentAtom.setColor(newColor);
                            gc.setFill(newColor);
                            gc.fillRect(i * size, j * size, size, size);
                            gc.strokeRect(i * size, j * size, size, size);
                        }
                    } else {
                        newColor = currentAtom.getTemperatureColor(newTemperature);
                        currentAtom.setColor(newColor);
                        gc.setFill(newColor);
                        gc.fillRect(i * size, j * size, size, size);
                        gc.strokeRect(i * size, j * size, size, size);
                    }

                }
            }
        }

        public void setWidth1(double width) {
            super.setWidth(width);
            this.n = calculateNumOfAtoms(width, getHeight());
            this.size = calculateAtomSize(width, getHeight(), this.n);
            this.grid = initializeGrid(n);
            redraw();
        }

        public void setHeight1(double height) {
            super.setHeight(height);
            this.n = calculateNumOfAtoms(getWidth(), height);
            this.size = calculateAtomSize(getWidth(), height, this.n);
            this.grid = initializeGrid(n);
            redraw();
        }

        private int calculateNumOfAtoms(double width, double height) {
            int n;
            if (width > height) {
                n = (int) (width / 8);
            } else {
                n = (int) (height / 8);
            }
            if (n < 1) {
                n = 1;
            }
            return n;
        }

        private int calculateAtomSize(double width, double height, int n) {
            int size;
            if (width > height) {
                size = (int) (width / n);
            } else {
                size = (int) (height / n);
            }
            if (size <= 1) {
                size ++;
            }
            return size;
        }

        private Atom[][] initializeGrid(int n) {
            Atom[][] grid = new Atom[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    grid[i][j] = new Atom(0);
                }
            }
            return grid;
        }

        private void redraw() {
            gc.clearRect(0, 0, getWidth(), getHeight());
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    Atom currentAtom = grid[i][j];
                    Color color = currentAtom.getColor();
                    gc.setFill(color);
                    gc.fillRect(i * size, j * size, size, size);
                    gc.strokeRect(i * size, j * size, size, size);
                }
            }
        }


        // RUNNABLE CLASS
        public static class SimulationTask implements Runnable {
            int start, end;
            double prevTemperature = 0;
            private CyclicBarrier barrier;

            public SimulationTask(int start, int end, CyclicBarrier barrier) {
                this.start = start;
                this.end = end;
                this.barrier  =barrier;
            }
            public void prije(){
                stable = true;
                double newTemperature = 0;
                for (int i = start; i < end; i++) {
                    for (int j = 0; j < n; j++) {
                        Atom currentAtom = grid[i][j];
                        prevTemperature = currentAtom.getTemperature();
                        currentAtom.setPrevTemperature(prevTemperature);
                        newTemperature = calculateNewTemperature(i, j);
                        if (newTemperature != prevTemperature && newTemperature!=0) {
                            currentAtom.setTemperature(newTemperature);
                            if ((Math.abs(newTemperature - prevTemperature)) > 0.25 && (prevTemperature != 100)) {
                                stable = false;
                            }
                        }
                    }
                }
                if(firstIteration){firstIteration = false;}
            }
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName());
                prije();
                try {
                    barrier.await(); // Wait for all threads to finish their computation
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }

            }

            public static void applyFixedTemperature(int i, int j, double temperature) {
                if(i<n && j<n){
                    grid[i][j].setTemperature(temperature);
                    grid[i][j].setPrevTemperature(temperature);
                    int up = (j + 1 + n) % n;
                    int right = (i + 1 + n) % n;
                    grid[i][up].setTemperature(temperature);
                    grid[right][j].setTemperature(temperature);
                    grid[right][up].setPrevTemperature(temperature);
                    grid[i][up].setPrevTemperature(temperature);
                    grid[right][j].setPrevTemperature(temperature);
                    grid[right][up].setPrevTemperature(temperature);
                }
            }

            public static double calculateNewTemperature(int i, int j) {
                int up = (j + 1 + n) % n;
                int down = (j - 1 + n) % n;
                int right = (i + 1 + n) % n;
                int left = (i - 1 + n) % n;
                if (up != n - 1 && down != 0 && right != 0 && left != n - 1) {
                    return (grid[i][up].getPrevTemperature() +
                            grid[right][j].getPrevTemperature() +
                            grid[i][down].getPrevTemperature() +
                            grid[left][j].getPrevTemperature()) / 4;
                } else if (left == n - 1) {
                    return (grid[i][up].getPrevTemperature() +
                            grid[right][j].getPrevTemperature() +
                            grid[i][down].getPrevTemperature()) / 3;
                } else if (right == 0) {
                    return (grid[i][up].getPrevTemperature() +
                            grid[left][j].getPrevTemperature() +
                            grid[i][down].getPrevTemperature()) / 3;
                } else if (up == n - 1) {
                    return (grid[right][j].getPrevTemperature() +
                            grid[i][down].getPrevTemperature() +
                            grid[left][j].getPrevTemperature()) / 3;
                } else if (down == 0) {
                    return (grid[right][j].getPrevTemperature() +
                            grid[i][down].getPrevTemperature() +
                            grid[left][j].getPrevTemperature()) / 3;
                } else {
                    return grid[i][j].getPrevTemperature();
                }
            }

        }
    }
}
