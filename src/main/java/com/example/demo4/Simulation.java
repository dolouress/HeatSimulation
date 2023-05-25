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
    boolean firstIteration;

    public Simulation(int n, int numOfHeat, int width, int height) {
        super(width, height);
        this.n = n; // num of atoms
        this.width = width;
        this.height = height;
        this.size = width/ n;
        this.grid = initializeGrid(n);
        this.gc = getGraphicsContext2D();
        this.numOfHeat = numOfHeat;
        this.firstIteration = true;

        if (arrayOfRandoms == null) {
            arrayOfRandoms = new int[2 * numOfHeat];
            for (int i = 0; i < numOfHeat * 2; i++) {
                int broj = rand.nextInt(n-1);
                arrayOfRandoms[i] = broj;
                System.out.println(broj);
            }
        }


        new AnimationTimer() {
            private long startTime = System.nanoTime();
            private boolean finished = false;
            private boolean stable =  false;
            private int index;

            @Override
            public void handle(long now) {
                if (finished) return;
                double newTemperature = 0;
                index = 0;

                if(!stable){
                    for(int i = 0; i<n;i++){
                        for (int j = 0;j<n;j++){
                            Atom currentAtom = grid[i][j];
                            Color newColor;
                            if(!firstIteration){
                                newTemperature = calculateNewTemperature(i, j);
                                newColor = currentAtom.getTemperatureColor(newTemperature);
                                if(newColor!=Color.BLUE) {
                                    currentAtom.setColor(newColor);
                                    gc.setFill(newColor);
                                    gc.fillRect(i * size, j * size, size, size);
                                    gc.strokeRect(i * size, j * size, size, size);
                                }
                            }
                            else{
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
                                if (newTemperature != prevTemperature && newTemperature>0.00001) {
                                    currentAtom.setTemperature(newTemperature);
                                    /*System.out.println("new temp:"+i+j+"temp:"+newTemperature);
                                    System.out.println("prev temp:"+i+j+"temp:"+prevTemperature);
                                    System.out.println(Math.abs(newTemperature - prevTemperature));
                                    System.out.println("------------------");*/
                                    if ((Math.abs(newTemperature - prevTemperature)) > 0.25 && (prevTemperature!=100)) {
                                        stable = false;
                                    }
                                }

                    }
                }
              /*  if(!stable){
                    System.out.println("-----------------------NOT STABLE END CYCLE---------------------");
                }*/

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
        grid[i][j].setPrevTemperature(temperature);
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
