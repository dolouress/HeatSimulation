package com.example.demo4;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Simulation extends Canvas {
    private static final long SEED = 1234; // Seed value for the random generator

    private int n, numOfHeat, size;
    private Atom[][] grid;
    private GraphicsContext gc;

    private static Random rand = new Random(SEED);
    private static int[] arrayOfRandoms;
    boolean firstIteration;

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

        if (arrayOfRandoms == null) {
            arrayOfRandoms = new int[2 * numOfHeat];
            for (int i = 0; i < numOfHeat * 2; i++) {
                int broj = rand.nextInt(n - 1);
                arrayOfRandoms[i] = broj;
                System.out.println(broj);
            }
        }

        new AnimationTimer() {
            private long startTime = System.nanoTime();
            private boolean finished = false;
            private boolean stable = false;
            private int index;

            @Override
            public void handle(long now) {
                if (finished) return;
                double newTemperature = 0;
                index = 0;

                if (!stable) {
                    for (int i = 0; i < n; i++) {
                        for (int j = 0; j < n; j++) {
                            Atom currentAtom = grid[i][j];
                            Color newColor;
                            if (!firstIteration) {
                                newTemperature = calculateNewTemperature(i, j);
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
                firstIteration = false;
                for (int k = 0; k < numOfHeat; k++) {
                    applyFixedTemperature(arrayOfRandoms[index], arrayOfRandoms[index + 1], 100);
                    index += 2;
                }

                stable = true;
                double prevTemperature = 0;
                for (int i = 0; i < n; i++) {
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

                if (stable) {
                    long endTime = System.nanoTime();
                    long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                    System.out.println("Simulation completed in " + duration + "ms");
                    finished = true;
                }
            }
        }.start();
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

    public void applyFixedTemperature(int i, int j, double temperature) {
        if(i<n && j<n){
            grid[i][j].setTemperature(temperature);
            grid[i][j].setPrevTemperature(temperature);
            int up = (j + 1 + n) % n;
            //int down = (j - 1 + n) % n;
            int right = (i + 1 + n) % n;
            //int left = (i - 1 + n) % n;
            grid[i][up].setTemperature(temperature);
            grid[right][j].setTemperature(temperature);
            grid[right][up].setPrevTemperature(temperature);
            //grid[i][down].setTemperature(temperature);
            //grid[left][j].setTemperature(temperature);
            grid[i][up].setPrevTemperature(temperature);
            grid[right][j].setPrevTemperature(temperature);
            grid[right][up].setPrevTemperature(temperature);
            //grid[i][down].setPrevTemperature(temperature);
            //grid[left][j].setPrevTemperature(temperature);
        }

    }

    private double calculateNewTemperature(int i, int j) {
        int up = (j + 1 + n) % n;
        int down = (j - 1 + n) % n;
        int right = (i + 1 + n) % n;
        int left = (i - 1 + n) % n;
        /*if(grid[i][j].getPrevTemperature()==100)
            return 100;
        /*double upTemp = grid[i][up].getPrevTemperature();
        double rightTemp = grid[right][j].getPrevTemperature();
        double downTemp = grid[i][down].getPrevTemperature();
        double leftTemp = grid[left][j].getPrevTemperature();*/
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

    private Atom[][] initializeGrid(int n) {
        Atom[][] grid = new Atom[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = new Atom(0);
            }
        }
        return grid;
    }

}
