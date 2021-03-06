package com.outliers.algo.aco;

import com.outliers.graph.Matrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Ant {
    private static final Logger log = LogManager.getLogger(Ant.class);

    private final int[] tabu;
    private final Map<Integer, Boolean> visited;
    private int curIdxInTabu = 0;
    private double tourLength;

    private final String name;
    private final Random rand;

    public Ant(int startNodeIdx, int numOfNodes) {
        this.tabu = new int[numOfNodes];
        this.visited = new HashMap<>();
        this.rand = new Random();

        this.name = "Ant " + startNodeIdx;
        visit(startNodeIdx);
    }

    public void moveToNext(Matrix adjMatrix, Matrix trails) {
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("Timeout.");
        }
        int nextNode = findNextNode(adjMatrix, trails);
        // Increment the tour length after finding the nextNode and BEFORE marking it as visited
        tourLength += adjMatrix.get(getCurNode(), nextNode);
        visit(nextNode);
    }

    public double getTourLength(Matrix adjMatrix) {
        // Full tour length is single path length + cost of edge from last to first node
        return this.tourLength + adjMatrix.get(getCurNode(), this.tabu[0]);
    }

    public String getName() {
        return this.name;
    }

    public int[] getTabu() {
        return this.tabu;
    }

    public String getPath() {
        return Arrays.stream(this.tabu)
            .mapToObj(String::valueOf)
            .collect(Collectors.joining("->"))
            .concat("->" + this.tabu[0]);
    }

    private int findNextNode(Matrix adjMatrix, Matrix trails) {
        Map<Integer, Double> distribution = new HashMap<>();
        double totalEdgeWeightage = 0.0;
        for (int i = 0; i < adjMatrix.getSize(); i++) {
            // Node is NOT yet visited
            if (!visited.containsKey(i)) {
                double edgeWeightage = calcEdgeWeightage(
                    adjMatrix.get(getCurNode(), i),
                    trails.get(getCurNode(), i)
                );
                totalEdgeWeightage += edgeWeightage;
                distribution.put(i, edgeWeightage);
            }
        }

        return getNextNodeByProbability(distribution, totalEdgeWeightage);
    }

    private Integer getNextNodeByProbability(Map<Integer, Double> distribution, double totalEdgeWeightage) {
        // Copied from https://stackoverflow.com/a/20329901/2950032
        // Verify this more
        double rand = this.rand.nextDouble();
        double ratio = 1.0f / totalEdgeWeightage;
        double tempDist = 0;
        for (Integer i : distribution.keySet()) {
            tempDist += distribution.get(i);
            if (rand / ratio <= tempDist) {
                return i;
            }
        }
        // TODO Will this ever happen?
        throw new RuntimeException("Vertex not found for some reason");
    }

    private int getCurNode() {
        return this.tabu[curIdxInTabu - 1];
    }

    private void visit(int idx) {
        if (curIdxInTabu == 0) {
            log.trace("{} starting on {}", name, idx);
        } else {
            log.trace("{} visited {}", name, idx);
        }
        this.tabu[curIdxInTabu++] = idx;
        this.visited.put(idx, true);
    }

    private double calcEdgeWeightage(double edgeCost, double trail) {
        return Math.pow(1.0f / edgeCost, Config.getEdgeWeightStrength()) * Math.pow(trail, Config.getPheromoneStrength());
    }
}
