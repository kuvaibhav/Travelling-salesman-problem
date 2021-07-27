package com.outliers.graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class Matrix {
    private static final Logger log = LogManager.getLogger(Matrix.class);

    private final double[][] matrix;
    private final int N;

    public Matrix(int n) {
        N = n;
        matrix = new double[n][n];
    }

    public double get(int i, int j) {
        validateIndices(i, j);
        return matrix[i][j];
    }

    public void set(int i, int j, double val) {
        validateIndices(i, j);
        matrix[i][j] = val;
    }

    private void validateIndices(int i, int j) {
        if (i < 0 || i >= N || j < 0 || j >= N) {
            log.error("Invalid indices: ({}, {})", i, j);
            throw new IllegalArgumentException("Invalid indices");
        }
    }

    public void print() {
        log.info("Size: {}", N);
        for (int i = 0; i < N; i++) {
            log.info("{}", Arrays.toString(matrix[i]));
        }
    }
    public int getSize(){
        return this.N;
    }
}
