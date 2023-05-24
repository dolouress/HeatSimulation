package com.example.demo4;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Simulation extends Canvas {
    private int n, numOfHeat, width, height, size;
    private int counter =0;
    private Atom[][] grid;
    private GraphicsContext gc;

    public List<Atom> changedAtoms = new ArrayList<>();

    public Simulation(int n, int numOfHeat, int width, int height){
        super(width,height);
        this.n = n; //num of atoms
        this.width = width;
        this.height = height;
        this.size = width / n;
        this.grid = initializeGrid(n);
        this.gc = getGraphicsContext2D();
        this.numOfHeat = numOfHeat;
        Random rand = new Random();
        int[] arrayOfRandoms = new int[2*numOfHeat];
        for(int i = 0; i< numOfHeat*2;i++){
            int broj = rand.nextInt(n);
            arrayOfRandoms[i] = broj;
            System.out.println(broj);
        }

        new AnimationTimer() {
            private long startTime = System.nanoTime();
            private boolean finished = false;


            @Override
            public void handle(long now) {
                if (finished) return;

                boolean stable = true;
                changedAtoms.clear(); // Clear the list of changed atoms for the current iteration

                int index = 0;
                for (int i = 0; i < numOfHeat; i++) {
                    int x = arrayOfRandoms[index];
                    int y = arrayOfRandoms[index + 1];
                    applyFixedTemperature(x, y, 100);
                    index += 2;
                }

                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        Atom atom = grid[i][j];
                        atom.setPrevTemperature(atom.getTemperature()); // Store the previous temperature

                        double newTemperature = calculateNewTemperature(i, j); // Calculate the new temperature

                        atom.setTemperature(newTemperature); // Update the temperature
                        changedAtoms.add(atom); // Add the atom to the list of changed atoms

                        if (Math.abs(atom.getTemperature() - atom.getPrevTemperature()) > 0.25) {
                            stable = false;
                        }
                    }
                }

                // Update the graphics only for the changed atoms
                for (Atom atom : changedAtoms) {
                    int i = atom.getX();
                    int j = atom.getY();
                    Color newColor = atom.getTemperatureColor(atom.getTemperature());
                    atom.setColor(newColor);
                    gc.setFill(newColor);
                    gc.fillRect(i * size, j * size, size, size);
                    gc.strokeRect(i * size, j * size, size, size);
                }
                changedAtoms.clear(); // Clear the list after updating the changed atoms


                if (stable) {
                    long endTime = System.nanoTime();
                    long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                    System.out.println("Simulation completed in " + duration + "ms");
                    finished = true;
                }
            }

            // Helper method to calculate the new temperature of an atom
            private double calculateNewTemperature(int i, int j) {
                int up = (j + 1 + n) % n;
                int down = (j - 1 + n) % n;
                int right = (i + 1 + n) % n;
                int left = (i - 1 + n) % n;

                double newTemperature = 0;

                if (up != n - 1 && down != 0 && right != 0 && left != n - 1) {
                    newTemperature = (grid[i][up].getPrevTemperature() +
                            grid[right][j].getPrevTemperature() +
                            grid[i][down].getPrevTemperature() +
                            grid[left][j].getPrevTemperature()) / 4;
                } else if (left == n - 1) {
                    newTemperature = (grid[i][up].getPrevTemperature() +
                            grid[right][j].getPrevTemperature() +
                            grid[i][down].getPrevTemperature()) / 3;
                } else if (right == 0) {
                    newTemperature = (grid[i][up].getPrevTemperature() +
                            grid[left][j].getPrevTemperature() +
                            grid[i][down].getPrevTemperature()) / 3;
                } else if (up == n - 1) {
                    newTemperature = (grid[right][j].getPrevTemperature() +
                            grid[i][down].getPrevTemperature() +
                            grid[left][j].getPrevTemperature()) / 3;
                } else if (down == 0) {
                    newTemperature = (grid[right][j].getPrevTemperature() +
                            grid[i][down].getPrevTemperature() +
                            grid[left][j].getPrevTemperature()) / 3;
                }

                return newTemperature;
            }

        }.start();

    }

    public void applyFixedTemperature(int i, int j, double temperature){
        grid[i][j].setTemperature(temperature);//oboji?
    }
    private Atom[][] initializeGrid(int n){
        Atom[][] grid = new Atom[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = new Atom(i,j,0, this);
            }
        }
        return grid;
    }
}
