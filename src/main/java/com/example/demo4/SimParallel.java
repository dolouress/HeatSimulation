//package com.example.demo4;
//import javafx.animation.AnimationTimer;
//import javafx.application.Application;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.scene.Scene;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.layout.StackPane;
//import javafx.scene.paint.Color;
//import javafx.stage.Stage;
//
//import java.util.Random;
//import java.util.concurrent.BrokenBarrierException;
//import java.util.concurrent.CyclicBarrier;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//
//public class SimParallel extends Application{
//
//    private static final long SEED = 1234; // Seed value for the random generator
//
//    private static final int WINDOW_WIDTH = 800; // Desired window width
//    private static final int WINDOW_HEIGHT = 600; // Desired window height
//    private static final int NUM_OF_HEAT = 10; // Number of heat sources
//    private static final int NUM_THREADS = 12; // Number of parallel threads
//
//    private static Random rand = new Random(SEED);
//    private static int[] arrayOfRandoms;
//
//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Simulation");
//
//        // Create the root container
//        StackPane root = new StackPane();
//        Simulation simulation = new Simulation(NUM_OF_HEAT, WINDOW_WIDTH, WINDOW_HEIGHT); // Pass the desired width and height to the Simulation constructor
//        root.getChildren().add(simulation);
//
//        // Create the scene with the root container
//        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
//        primaryStage.setScene(scene);
//
//        // Update the width and height properties when the window is resized
//        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
//            double newWidth = newValue.doubleValue();
//            simulation.setWidth1(newWidth);
//        });
//
//        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
//            double newHeight = newValue.doubleValue();
//            simulation.setHeight1(newHeight);
//        });
//
//        primaryStage.setResizable(true); // Allow manual window resizing
//
//        primaryStage.show();
//    }
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    public class Simulation extends Canvas {
//        private int n, numOfHeat, size;
//        private Atom[][] grid;
//        private GraphicsContext gc;
//
//        private boolean finished = false;
//        private boolean stable = false;
//        private int index;
//        private boolean firstIteration = true;
//
//        public Simulation(int numOfHeat, double width, double height) {
//            super(width, height);
//            this.n = calculateNumOfAtoms(width, height);
//            this.size = calculateAtomSize(width, height, this.n);
//            this.gc = getGraphicsContext2D();
//            this.grid = initializeGrid(n);
//            this.numOfHeat = numOfHeat;
//            this.firstIteration = true;
//
//            this.setWidth(width);
//            this.setHeight(height);
//
//
//            if (arrayOfRandoms == null) {
//                arrayOfRandoms = new int[2 * numOfHeat];
//                for (int i = 0; i < numOfHeat * 2; i++) {
//                    int broj = rand.nextInt(n - 1);
//                    arrayOfRandoms[i] = broj;
//                    System.out.println(broj);
//                }
//            }
//
//            ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
//
//            new AnimationTimer() {
//                private long startTime = System.nanoTime();
//
//                @Override
//                public void handle(long now) {
//                    if (!stable) {
//                        for (int i = 0; i < NUM_THREADS; i++) {
//                            int start = i * (n / NUM_THREADS);
//                            int end = (i + 1) * (n / NUM_THREADS);
//                            executorService.execute(new SimulationTask(start, end, barrier));
//                        }
//                    } else {
//                        executorService.shutdown();
//                        try {
//                            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        long endTime = System.nanoTime();
//                        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
//                        System.out.println("Simulation finished in " + elapsedTime + " seconds.");
//                    }
//                }
//            }.start();
//        }
//
//        public void setWidth1(double width) {
//            super.setWidth(width);
//            this.n = calculateNumOfAtoms(width, getHeight());
//            this.size = calculateAtomSize(width, getHeight(), this.n);
//            this.grid = initializeGrid(n);
//            redraw();
//        }
//
//        public void setHeight1(double height) {
//            super.setHeight(height);
//            this.n = calculateNumOfAtoms(getWidth(), height);
//            this.size = calculateAtomSize(getWidth(), height, this.n);
//            this.grid = initializeGrid(n);
//            redraw();
//        }
//
//        private int calculateNumOfAtoms(double width, double height) {
//            int minDimension = (int) Math.min(width, height);
//            return minDimension / 20; // Adjust the divisor based on your preference
//        }
//
//        private int calculateAtomSize(double width, double height, int n) {
//            int minDimension = (int) Math.min(width, height);
//            return minDimension / n;
//        }
//
//        private Atom[][] initializeGrid(int n) {
//            Atom[][] grid = new Atom[n][n];
//            for (int i = 0; i < n; i++) {
//                for (int j = 0; j < n; j++) {
//                    grid[i][j] = new Atom(0);
//                }
//            }
//            return grid;
//        }
//
//        private void redraw() {
//            if (stable) {
//                System.out.println("Simulation finished!");
//            }
//            gc.clearRect(0, 0, getWidth(), getHeight());
//            for (int i = 0; i < n; i++) {
//                for (int j = 0; j < n; j++) {
//                    Atom currentAtom = grid[i][j];
//                    Color color = currentAtom.getColor();
//                    gc.setFill(color);
//                    gc.fillRect(i * size, j * size, size, size);
//                    gc.strokeRect(i * size, j * size, size, size);
//                }
//            }
//        }
//        private class SimulationTask implements Runnable {
//            int start, end;
//            boolean stable = true;
//            double prevTemperature = 0;
//            //CyclicBarrier c;
//            private CyclicBarrier barrier;
//
//            public SimulationTask(int start, int end, CyclicBarrier barrier) {
//                this.start = start;
//                this.end = end;
//                this.barrier = barrier;
//            }
//            @Override
//            public void run() {
//                double newTemperature = 0;
//                for (int i = start; i < end; i++) {
//                    for (int j = 0; j < n; j++) {
//                        Atom currentAtom = grid[i][j];
//                        prevTemperature = currentAtom.getTemperature();
//                        currentAtom.setPrevTemperature(prevTemperature);
//                        newTemperature = calculateNewTemperature(i, j);
//                        if (newTemperature != prevTemperature && newTemperature!=0) {
//                            currentAtom.setTemperature(newTemperature);
//                            if ((Math.abs(newTemperature - prevTemperature)) > 0.25 && (prevTemperature != 100)) {
//                                stable = false;
//                            }
//                        }
//                    }
//                }
//                try {
//                    barrier.await(); // Wait for all threads to reach the barrier
//                } catch (InterruptedException | BrokenBarrierException e) {
//                    e.printStackTrace();
//                }
//
//                if (firstIteration) {
//                    firstIteration = false;
//                }
//        }
//
//            private boolean isHeatSource(int i, int j) {
//                for (int k = 0; k < NUM_OF_HEAT; k++) {
//                    if (i == arrayOfRandoms[k] && j == arrayOfRandoms[k + NUM_OF_HEAT]) {
//                        return true;
//                    }
//                }
//                return false;
//            }
//            private double calculateNewTemperature(int i, int j) {
//                int up = (j + 1 + n) % n;
//                int down = (j - 1 + n) % n;
//                int right = (i + 1 + n) % n;
//                int left = (i - 1 + n) % n;
//                if (up != n - 1 && down != 0 && right != 0 && left != n - 1) {
//                    return (grid[i][up].getPrevTemperature() +
//                            grid[right][j].getPrevTemperature() +
//                            grid[i][down].getPrevTemperature() +
//                            grid[left][j].getPrevTemperature()) / 4;
//                } else if (left == n - 1) {
//                    return (grid[i][up].getPrevTemperature() +
//                            grid[right][j].getPrevTemperature() +
//                            grid[i][down].getPrevTemperature()) / 3;
//                } else if (right == 0) {
//                    return (grid[i][up].getPrevTemperature() +
//                            grid[left][j].getPrevTemperature() +
//                            grid[i][down].getPrevTemperature()) / 3;
//                } else if (up == n - 1) {
//                    return (grid[right][j].getPrevTemperature() +
//                            grid[i][down].getPrevTemperature() +
//                            grid[left][j].getPrevTemperature()) / 3;
//                } else if (down == 0) {
//                    return (grid[right][j].getPrevTemperature() +
//                            grid[i][down].getPrevTemperature() +
//                            grid[left][j].getPrevTemperature()) / 3;
//                } else {
//                    return grid[i][j].getPrevTemperature();
//                }
//            }
//
//
//        }
//    }
//}
