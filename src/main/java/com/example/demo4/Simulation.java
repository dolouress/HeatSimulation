package com.example.demo4;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Simulation extends Canvas {
    private int n, numOfHeat, width, height, size;
    private int counter =0;
    private Atom[][] grid;
    private GraphicsContext gc;

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

        new AnimationTimer(){//per frame
            private long startTime = System.nanoTime();
            private boolean finished = false;

            @Override
            public void handle(long now) {
                if (finished) return;

                boolean stable = true;
                //setEdgeTemperatureToZero();//setting edges to zero, so it doesn't go to the other sides

                //adding fixed temperatures to some atoms
                //applyFixedTemperature(rand.nextInt(n), rand.nextInt(n),100);
                /*applyFixedTemperature(50, 20,100);
                applyFixedTemperature(0, 25,100);
                applyFixedTemperature(5, 28,100);*/


                int index = 0;
                for(int i =0;i<numOfHeat;i++){
                    applyFixedTemperature(arrayOfRandoms[index], arrayOfRandoms[index+1],100);
                    index+=2;
                }
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        grid[i][j].setPrevTemperature(grid[i][j].getTemperature());
                    }
                }
                double newTemperature = 0;
                //pitaj. kako da podijelim grid na threadove pa ako imam nor dodiejljeno 8 tredova
                //ili4.. kako da podijelim na taj odredjen broj i sta ako je neparan broj
                //i kako da paralelizujem a grid const
                int up, down, right, left;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        up = (j + 1 + n) % n;
                        down = (j - 1 + n) % n;
                        right = (i + 1 + n) % n;
                        left = (i - 1 + n) % n;
                        if(up!=n-1 && down!=0 && right!=0 && left!=n-1) {
                            newTemperature = (grid[i][up].getPrevTemperature() +
                                    grid[right][j].getPrevTemperature() +
                                    grid[i][down].getPrevTemperature() +
                                    grid[left][j].getPrevTemperature()) / 4;
                        }
                        else if (left == n - 1) {
                                newTemperature = (grid[i][up].getPrevTemperature() +
                                        grid[right][j].getPrevTemperature() +
                                        grid[i][down].getPrevTemperature()) / 3;
                        } else if (right == 0) {
                            newTemperature = (grid[i][up].getPrevTemperature() +
                                    grid[left][j].getPrevTemperature() +
                                    grid[i][down].getPrevTemperature()) / 3;
                        } else if (up==n-1) {
                            newTemperature = (grid[right][j].getPrevTemperature() +
                                    grid[i][down].getPrevTemperature() +
                                    grid[left][j].getPrevTemperature()) / 3;
                        }else if(down ==0){
                            newTemperature = (grid[right][j].getPrevTemperature() +
                                    grid[i][down].getPrevTemperature() +
                                    grid[left][j].getPrevTemperature()) / 3;
                        }


                        grid[i][j].setTemperature(newTemperature);

                        //TRIAL
                        Color newColor = grid[i][j].getTemperatureColor(newTemperature);
                        grid[i][j].setColor(newColor);
                        gc.setFill(grid[i][j].getColor());
                        gc.fillRect(i*size,j*size,size,size);
                        gc.strokeRect(i*size,j*size,size,size);
                       //END TRIAL


                        if (Math.abs(grid[i][j].getTemperature() - grid[i][j].getPrevTemperature()) > 25) {
                            stable = false;
                        }

                    }

                }
                if (stable) {
                    long endTime = System.nanoTime();
                    long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                    System.out.println("Simulation completed in " + duration + "ms");
                    finished = true;
                }



/*
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        Color color = grid[i][j].getTemperatureColor(grid[i][j].getTemperature());
                        grid[i][j].setColor(color);
                        gc.setFill(grid[i][j].getColor());
                        gc.fillRect(i*size,j*size,size,size);
                        gc.strokeRect(i*size,j*size,size,size);
                    }
                }
*/
            }
        }.start();
    }
    private void setEdgeTemperatureToZero() {
        for (int i = 0; i < n; i++) {
            grid[i][0].setTemperature(0);
            grid[i][n-1].setTemperature(0);
        }
        for (int j = 0; j < n; j++) {
            grid[0][j].setTemperature(0);
            grid[n-1][j].setTemperature(0);
        }
    }
    public void applyFixedTemperature(int i, int j, double temperature){
        grid[i][j].setTemperature(temperature);//oboji?
    }
    private Atom[][] initializeGrid(int n){
        Atom[][] grid = new Atom[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = new Atom(0);
            }
        }
        return grid;
    }
}
