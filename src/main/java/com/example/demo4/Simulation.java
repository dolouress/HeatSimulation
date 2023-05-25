package com.example.demo4;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Simulation extends Canvas {
    private int n, numOfHeat, width, height, size;
    private Atom[][] grid;
    private GraphicsContext gc;

    private static Random rand = new Random();
    private static int[] arrayOfRandoms;

    public Simulation(int n, int numOfHeat, int width, int height) {
        super(width, height);
        this.n = n; // num of atoms
        this.width = width;
        this.height = height;
        this.size = width / n;
        this.grid = initializeGrid(n);
        this.gc = getGraphicsContext2D();
        this.numOfHeat = numOfHeat;

        if (arrayOfRandoms == null) {
            arrayOfRandoms = new int[2 * numOfHeat];
            for (int i = 0; i < numOfHeat * 2; i++) {
                int broj = rand.nextInt(n);
                arrayOfRandoms[i] = broj;
                System.out.println(broj);
            }
        }

        new AnimationTimer() {
            private long startTime = System.nanoTime();
            private boolean finished = false;
            boolean stable ;
            int index;

            @Override
            public void handle(long now) {
                if (finished) return;

                stable = true;

                index = 0;
                for (int i = 0; i < numOfHeat; i++) {
                    applyFixedTemperature(arrayOfRandoms[index], arrayOfRandoms[index + 1], 100);
                    index += 2;
                }

                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        grid[i][j].setPrevTemperature(grid[i][j].getTemperature());
                    }
                }
                double newTemperature = 0;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        Atom currentAtom = grid[i][j];
                        newTemperature = calculateNewTemperature(i, j);
                        if (newTemperature != currentAtom.getPrevTemperature()) {
                            currentAtom.setTemperature(newTemperature);
                            Color newColor = currentAtom.getTemperatureColor(newTemperature);
                            currentAtom.setColor(newColor);
                            gc.setFill(newColor);
                            gc.fillRect(i * size, j * size, size, size);
                            gc.strokeRect(i * size, j * size, size, size);
                            if (Math.abs(newTemperature - currentAtom.getPrevTemperature()) > 0.25) {
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

    public void applyFixedTemperature(int i, int j, double temperature) {
        grid[i][j].setTemperature(temperature);
    }

    private double calculateNewTemperature(int i, int j) {
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
