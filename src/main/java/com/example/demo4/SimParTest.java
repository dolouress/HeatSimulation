package com.example.demo4;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimParTest extends Application {
    private static final long SEED = 1234;
    private static final int NUM_THREADS = 8;

    private int width=800;
    private int height=500;
    private int numOfHeat=10;

    private static Random rand = new Random(SEED);
    private static int[] arrayOfRandoms;

    public SimParTest() {}

    public SimParTest(int width, int height, int numOfHeat) {
        this.width = width;
        this.height = height;
        this.numOfHeat = numOfHeat;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simulation");

        StackPane root = new StackPane();
        SimulationT simulation = new SimulationT(numOfHeat, width, height);
        root.getChildren().add(simulation);

        Scene scene = new Scene(root, width, height);
        primaryStage.setScene(scene);

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = newValue.doubleValue();
            simulation.setWidth(newWidth);
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            double newHeight = newValue.doubleValue();
            simulation.setHeight(newHeight);
        });

        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static class SimulationT extends Canvas {
        private static final int GRID_SIZE = 8;

        private int n;
        private int size;
        private Atom[][] grid;
        private GraphicsContext gc;
        private boolean firstIteration = true;
        private AtomicBoolean globalStable = new AtomicBoolean(false);
        private AtomicBoolean[] stableFlags;

        public SimulationT(int numOfHeat, double width, double height) {
            super(width, height);
            this.n = calculateNumOfAtoms(width, height);
            this.size = calculateAtomSize(width, height, this.n);
            this.gc = getGraphicsContext2D();
            this.grid = initializeGrid(n);
            this.stableFlags = new AtomicBoolean[NUM_THREADS];
            this.firstIteration = true;

            initializeHeatSources(numOfHeat);

            ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
            CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);

            new AnimationTimer() {
                private long startTime = System.nanoTime();

                @Override
                public void handle(long now) {
                    if (globalStable.get()) {
                        long endTime = System.nanoTime();
                        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                        System.out.println("Simulation completed in " + duration + "ms");
                        executorService.shutdown();
                        try {
                            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                                executorService.shutdownNow();
                            }
                        } catch (InterruptedException e) {
                            executorService.shutdownNow();
                        }
                        return;
                    }

                    if (!firstIteration) {
                        globalStable.set(true);
                        for (int i = 0; i < NUM_THREADS; i++) {
                            if (!stableFlags[i].get()) {
                                globalStable.set(false);
                                break;
                            }
                        }
                    }

                    paint();
                    fixed();

                    if (firstIteration) {
                        firstIteration = false;
                    }

                    for (int i = 0; i < NUM_THREADS; i++) {
                        int finalI = i;
                        executorService.submit(() -> simulate(barrier, finalI));
                    }
                }
            }.start();
        }

        private void simulate(CyclicBarrier barrier, int threadIndex) {
            int start = threadIndex * (n / NUM_THREADS);
            int end = (threadIndex + 1) * (n / NUM_THREADS);

            for (int i = start; i < end; i++) {
                for (int j = 0; j < n; j++) {
                    calculateNewTemperature(i, j);
                }
            }

            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }

        private void paint() {
            gc.clearRect(0, 0, getWidth(), getHeight());

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    gc.setFill(grid[i][j].color);
                    gc.fillRect(i * size, j * size, size, size);
                }
            }
        }

        private void fixed() {
            for (int i = 0; i < NUM_THREADS; i++) {
                stableFlags[i] = new AtomicBoolean(true);
            }
        }

        private void calculateNewTemperature(int x, int y) {
            Atom atom = grid[x][y];

            if (!atom.isHeatSource) {
                int numNeighbors = 0;
                double averageTemperature = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int neighborX = x + i;
                        int neighborY = y + j;

                        if (neighborX >= 0 && neighborX < n && neighborY >= 0 && neighborY < n) {
                            Atom neighbor = grid[neighborX][neighborY];
                            averageTemperature += neighbor.temperature;
                            numNeighbors++;
                        }
                    }
                }

                averageTemperature /= numNeighbors;

                atom.temperature = averageTemperature;

                if (Math.abs(atom.temperature - atom.oldTemperature) >= 0.1) {
                    stableFlags[x % NUM_THREADS].set(false);
                }

                atom.oldTemperature = atom.temperature;
            }
        }

        private void initializeHeatSources(int numOfHeat) {
            for (int i = 0; i < numOfHeat; i++) {
                int x = rand.nextInt(n);
                int y = rand.nextInt(n);
                grid[x][y].isHeatSource = true;
            }
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
                    grid[i][j] = new Atom();
                }
            }

            return grid;
        }

        public void setWidth1(double width) {
            this.setWidth(width);
            this.size = calculateAtomSize(width, getHeight(), n);
        }

        public void setHeight1(double height) {
            this.setHeight(height);
            this.size = calculateAtomSize(getWidth(), height, n);
        }

        private static class Atom {
            private boolean isHeatSource;
            private double temperature;
            private double oldTemperature;
            private Color color;

            public Atom() {
                this.isHeatSource = false;
                this.temperature = 0;
                this.oldTemperature = 0;
                this.color = Color.WHITE;
            }
        }
    }

    public static void main(String[] args) {
        int width = 800;
        int height = 600;
        int numOfHeat = 5;
        arrayOfRandoms = new int[numOfHeat];

        for (int i = 0; i < numOfHeat; i++) {
            arrayOfRandoms[i] = rand.nextInt();
        }

        SimulationParallel simulationParallel = new SimulationParallel(width, height, numOfHeat);
        simulationParallel.launch(args);
    }
}
