package com.cyborgmas.isingmodel;

import static com.cyborgmas.isingmodel.IsingModel.RANDOM;

public class Simulation3D {
    private final int size;
    public final int[][][] model;
    private final int flipsToDo;

    public Simulation3D(int size) {
        this(size, 100*size*size*size);
    }

    public Simulation3D(int size, int flipsToDo) {
        this.size = size;
        this.flipsToDo = flipsToDo;
        this.model = new int[size][size][size];
        this.randomize();
    }

    public void randomize() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    this.model[i][j][k] = RANDOM.nextBoolean() ? -1 : 1;
                }
            }
        }
    }

    public void simulate(double temperature) {
        long start = System.currentTimeMillis();

        for (int i = 0; i <= flipsToDo; i++) {
            int x = RANDOM.nextInt(size);
            int y = RANDOM.nextInt(size);
            int z = RANDOM.nextInt(size);
            int otherSpins = nbOfNeighbours(x, y, z);
            if (otherSpins >= 3) { //here delta E is <=0, so flip
                model[x][y][z] *= -1;
                continue;
            }
            double prob = Math.exp(-4 * (3 - otherSpins)/ temperature);
            boolean flip = prob > RANDOM.nextDouble(); //next double is a random nb btwn 0 and 1.
            if (flip)
                model[x][y][z] *= -1;
        }

        double seconds = (System.currentTimeMillis() - start) / 1000D;
        System.out.println(seconds);
    }

    //Counts non-matching neighbours
    public int nbOfNeighbours(int x, int y, int z) {
        int ret = 0;
        int spin = model[x][y][z];

        if (x < (size - 1)) { //non boundary
            if (spin != model[x+1][y][z])
                ret++;
        } else if (spin != model[0][y][z]) //boundary
            ret ++;

        if (x > 0) { //non boundary
            if (spin != model[x-1][y][z])
                ret++;
        }
        else if (spin != model[(size - 1)][y][z]) //boundary
            ret++;

        if (y < (size - 1)) { //non boundary
            if (spin != model[x][y+1][z])
                ret++;
        } else if (spin != model[x][0][z]) //boundary
            ret++;

        if (y > 0) { //non boundary
            if (spin != model[x][y-1][z])
                ret++;
        }
        else if (spin != model[x][(size - 1)][z]) //boundary
            ret++;

        if (z < (size - 1)) { //non boundary
            if (spin != model[x][y][z+1])
                ret++;
        } else if (spin != model[x][y][0]) //boundary
            ret++;

        if (z > 0) { //non boundary
            if (spin != model[x][y][z-1])
                ret++;
        }
        else if (spin != model[x][y][(size - 1)]) //boundary
            ret++;

        return ret;
    }
}
