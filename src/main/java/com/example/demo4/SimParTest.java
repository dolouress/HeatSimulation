/*package com.example.demo4;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimParTest extends Application {
    private static final long SEED = 1234; // Seed value for the random generator
    private static final int NUM_THREADS = 12; // Number of parallel threads
    private static Random rand = new Random(SEED);
    private static int[] arrayOfRandoms;
    private int width;
    private int height;
    private int numOfHeat;

    public SimParTest() {
    }

    public SimParTest(int width, int height, int numOfHeat) {
        this.width = width;
        this.height = height;
        this.numOfHeat = numOfHeat;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simulation");

        // Create the root container
        StackPane root = new StackPane();
        Simulation simulation = new Simulation(numOfHeat, width, height); // Pass the desired width and height to the Simulation constructor
        root.getChildren().add(simulation);

        // Create the scene with the root container
        Scene scene = new Scene(root, width, height);
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

    public static class Simulation extends Canvas {
        private int n;
        private int numOfHeat;
        private int size;
        private Atom[][] grid;
        private GraphicsContext gc;
        private boolean finished = false;
        private static AtomicBoolean globalStable = new AtomicBoolean(false);
        private int index;
        private static AtomicBoolean[] stableFlags;
        private ExecutorService executorService;
        private CyclicBarrier barrier;
        private boolean firstIteration = true;

        public Simulation(int numOfHeat, double width, double height) {
            super(width, height);
            this.n = calculateNumOfAtoms(width, height);
            this.size = calculateAtomSize(width, height, this.n);
            this.gc = getGraphicsContext2D();
            this.grid = initializeGrid(n);
            this.numOfHeat = numOfHeat;
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

            // Initialize stableFlags array
            stableFlags = new AtomicBoolean[NUM_THREADS];
            for (int i = 0; i < NUM_THREADS; i++) {
                stableFlags[i] = new AtomicBoolean(false);
            }

            executorService = Executors.newFixedThreadPool(NUM_THREADS);
            barrier = new CyclicBarrier(NUM_THREADS);

            new AnimationTimer() {
                private long startTime = System.nanoTime();

                @Override
                public void handle(long now) {
                    if (finished) {
                        return;
                    }

                    // Calculate and update the state of atoms
                    if (firstIteration) {
                        for (int i = 0; i < NUM_THREADS; i++) {
                            executorService.execute(new SimulationTask(i));
                        }
                        firstIteration = false;
                    } else {
                        executorService.execute(new SimulationTask(index));
                    }

                    // Check if all threads have finished
                    boolean allThreadsFinished = true;
                    for (int i = 0; i < NUM_THREADS; i++) {
                        if (!stableFlags[i].get()) {
                            allThreadsFinished = false;
                            break;
                        }
                    }

                    if (allThreadsFinished) {
                        // Check if the system has reached a stable state
                        boolean isStable = globalStable.get();
                        if (isStable) {
                            long elapsedTime = System.nanoTime() - startTime;
                            System.out.println("Simulation finished in " + elapsedTime + " nanoseconds");
                            executorService.shutdownNow();
                            finished = true;
                        } else {
                            // Reset stableFlags for the next iteration
                            for (int i = 0; i < NUM_THREADS; i++) {
                                stableFlags[i].set(false);
                            }
                            startTime = System.nanoTime();
                        }
                    }
                }
            }.start();
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


        private Atom[][] initializeGrid(int numOfAtoms) {
            // Initialize the grid with atoms
            Atom[][] grid = new Atom[n][n];
            for (int i = 0; i < numOfAtoms; i++) {
                int x = i % n;
                int y = i / n;
                grid[x][y] = new Atom(0);
            }
            return grid;
        }

        public void setWidth1(double width) {
            this.setWidth(width);
            this.size = calculateAtomSize(width, getHeight(), this.n);
        }

        public void setHeight1(double height) {
            this.setHeight(height);
            this.size = calculateAtomSize(getWidth(), height, this.n);
        }

        private class SimulationTask implements Runnable {
            private int threadIndex;

            public SimulationTask(int threadIndex) {
                this.threadIndex = threadIndex;
            }

            @Override
            public void run() {
                try {
                    // Perform simulation for the assigned portion of the grid
                    int startIndex = threadIndex * n / NUM_THREADS;
                    int endIndex = (threadIndex + 1) * n / NUM_THREADS;
                    simulate(startIndex, endIndex);

                    // Wait for all threads to finish this iteration
                    barrier.await();

                    // Update the stable flag for this thread
                    boolean isStable = checkStable(startIndex, endIndex);
                    stableFlags[threadIndex].set(isStable);

                    // Wait for all threads to update their stable flags
                    barrier.await();

                    // Update the global stable flag
                    boolean allThreadsStable = true;
                    for (int i = 0; i < NUM_THREADS; i++) {
                        if (!stableFlags[i].get()) {
                            allThreadsStable = false;
                            break;
                        }
                    }
                    globalStable.set(allThreadsStable);

                    // Wait for all threads to update the global stable flag
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }

            private void simulate(int startIndex, int endIndex) {
                for (int x = startIndex; x < endIndex; x++) {
                    for (int y = 0; y < n; y++) {
                        // Perform simulation for each atom in the assigned portion
                        Atom atom = grid[x][y];
                        atom.updateVelocity();
                        atom.updatePosition(size, getWidth(), getHeight());
                    }
                }
            }
            public void simulate(){
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
                                localStable = false;
                            }
                        }
                    }
                }
                if(firstIteration){firstIteration = false;}
            }

            private boolean checkStable(int startIndex, int endIndex) {
                for (int x = startIndex; x < endIndex; x++) {
                    for (int y = 0; y < n; y++) {
                        Atom atom = grid[x][y];
                        if (!atom.isStable()) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
    }

    public static void main(String[] args) {
        Simulation simulation = new Simulation(100, 800, 600);
        simulation.start();
    }
}

*/