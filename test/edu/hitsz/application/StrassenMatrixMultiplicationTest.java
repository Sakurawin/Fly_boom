package edu.hitsz.application;

import edu.hitsz.StrassenMatrixMultiplication;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StrassenMatrixMultiplicationTest {

    @Test
    void multiply() {
        StrassenMatrixMultiplication smm = new StrassenMatrixMultiplication();

        // Test case 1: 2x2 matrices
        int[][] A2x2 = {{1, 2}, {3, 4}};
        int[][] B2x2 = {{5, 6}, {7, 8}};
        int[][] expected2x2 = {{19, 22}, {43, 50}};
        assertArrayEquals(expected2x2, smm.multiply(A2x2, B2x2));

        // Test case 3: 1x1 matrices
        int[][] A1x1 = {{5}};
        int[][] B1x1 = {{6}};
        int[][] expected1x1 = {{30}};
        assertArrayEquals(expected1x1, smm.multiply(A1x1, B1x1));
    }

    @Test
    void sub() {
        StrassenMatrixMultiplication smm = new StrassenMatrixMultiplication();
        int[][] A = {{5, 6}, {7, 8}};
        int[][] B = {{1, 2}, {3, 4}};
        int[][] expected = {{4, 4}, {4, 4}};
        assertArrayEquals(expected, smm.sub(A, B));
    }

    @Test
    void add() {
        StrassenMatrixMultiplication smm = new StrassenMatrixMultiplication();
        int[][] A = {{1, 2}, {3, 4}};
        int[][] B = {{5, 6}, {7, 8}};
        int[][] expected = {{6, 8}, {10, 12}};
        assertArrayEquals(expected, smm.add(A, B));
    }

    @Test
    void split() {
        StrassenMatrixMultiplication smm = new StrassenMatrixMultiplication();
        int[][] parent = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 16}};
        int n = parent.length;
        int[][] child = new int[n / 2][n / 2];

        // Split A11
        int[][] expectedA11 = {{1, 2}, {5, 6}};
        smm.split(parent, child, 0, 0);
        assertArrayEquals(expectedA11, child);

        // Split A12
        int[][] expectedA12 = {{3, 4}, {7, 8}};
        smm.split(parent, child, 0, n / 2);
        assertArrayEquals(expectedA12, child);

        // Split A21
        int[][] expectedA21 = {{9, 10}, {13, 14}};
        smm.split(parent, child, n / 2, 0);
        assertArrayEquals(expectedA21, child);

        // Split A22
        int[][] expectedA22 = {{11, 12}, {15, 16}};
        smm.split(parent, child, n / 2, n / 2);
        assertArrayEquals(expectedA22, child);
    }

    @Test
    void join() {
        StrassenMatrixMultiplication smm = new StrassenMatrixMultiplication();
        int[][] C11 = {{1, 2}, {5, 6}};
        int[][] C12 = {{3, 4}, {7, 8}};
        int[][] C21 = {{9, 10}, {13, 14}};
        int[][] C22 = {{11, 12}, {15, 16}};
        int[][] parent = new int[4][4];

        smm.join(C11, parent, 0, 0);
        smm.join(C12, parent, 0, 2);
        smm.join(C21, parent, 2, 0);
        smm.join(C22, parent, 2, 2);

        int[][] expected = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 16}};
        assertArrayEquals(expected, parent);
    }
}