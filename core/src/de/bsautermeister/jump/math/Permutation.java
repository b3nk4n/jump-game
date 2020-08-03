package de.bsautermeister.jump.math;

import com.badlogic.gdx.math.MathUtils;

public class Permutation {

    private int position;
    private final int[] data;

    public Permutation(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException("Min-value must be smaller or equal max-value");
        }

        data = new int[max - min + 1];
        for (int i = 0; i < data.length; ++i) {
            data[i] = min + i;
        }
        shuffleArray(data);
    }

    public int next() {
        return data[position++];
    }

    private static void shuffleArray(int[] array)
    {
        int index, temp;
        for (int i = array.length - 1; i > 0; i--)
        {
            index = MathUtils.random(i);
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

}
