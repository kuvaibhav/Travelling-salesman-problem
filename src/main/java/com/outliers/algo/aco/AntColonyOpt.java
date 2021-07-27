package com.outliers.algo.aco;

import com.outliers.algo.Algorithm;
import com.outliers.graph.Graph;
import com.outliers.graph.Matrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AntColonyOpt implements Algorithm {
    private static final Logger log = LogManager.getLogger(AntColonyOpt.class);
    private final int USELESS_CYCLES_THRESHOLD;

    private final Matrix adjMatrix;
    private final int numOfNodes;
    private final int numOfCycles;
    private int curCycle = 0;
    private Matrix trails;
    private Ant[] ants;
    private double bestTourLength = Double.MAX_VALUE; // TODO Maybe move it to MST upper bound?
    private String bestTourPath;
    private int numOfUselessCycles = 0;

    public AntColonyOpt(Graph graph) {
        adjMatrix = graph.getMatrix();
        numOfNodes = adjMatrix.getSize();
        USELESS_CYCLES_THRESHOLD = numOfNodes > 100 ? 20 : 2000; // Have threshold only for > 100
        numOfCycles = 2000; // TODO Maybe calculate based on the numOfNodes?
        initTrails();
    }

    @Override
    public void run() {
        while (shouldContinue()) {
            log.debug("Starting cycle {}", curCycle);
            initAnts();

            for (int i = 1; i < numOfNodes; i++) {
                for (Ant ant : ants) {
                    ant.moveToNext(adjMatrix, trails);
                }
            }
            findBestTour();
            evaporate();
            updateTrails();
        }
        log.info("Best found TSP solution of cost {} visiting {}", bestTourLength, bestTourPath);
    }

    @Override
    public List<String> getValues() {
        return new ArrayList<>(Arrays.asList(String.valueOf(bestTourLength), String.valueOf(curCycle - 1)));
    }

    private void findBestTour() {
        boolean isBetterFound = false;
        for (Ant ant : ants) {
            double antTourLength = ant.getTourLength(adjMatrix);
            if (bestTourLength > antTourLength) {
                isBetterFound = true;
                bestTourLength = antTourLength;
                bestTourPath = ant.getPath();
            }
            if (log.isTraceEnabled()) {
                log.trace("{} found cycle {} with cost {}", ant.getName(), ant.getPath(), antTourLength);
            }
        }
        if (isBetterFound) {
            numOfUselessCycles = 0;
            log.info("New best found TSP solution of cost {} visiting {}", bestTourLength, bestTourPath);
        } else {
            numOfUselessCycles++;
        }
    }

    private void evaporate() {
        for (int i = 0; i < numOfNodes; i++) {
            for (int j = i + 1; j < numOfNodes; j++) {
                trails.set(i, j, trails.get(i, j) * Config.getRateOfEvaporation());
                trails.set(j, i, trails.get(j, i) * Config.getRateOfEvaporation());
            }
        }
    }

    private void updateTrails() {
        for (Ant ant : ants) {
            double pheromone = Config.getQ3() / ant.getTourLength(adjMatrix);
            int[] tabu = ant.getTabu();

            // tabu.length - 1 is important because we don't want to go till the last node
            // and then overflow the i + 1 value
            for (int i = 0; i < tabu.length - 1; i++) {
                int u = tabu[i], v = tabu[i + 1];
                // Add pheromone to the edges
                trails.set(u, v, trails.get(u, v) + pheromone);
                // FIXME: Would be better to add this symmetric functionality in the Matrix class itself?
                trails.set(v, u, trails.get(v, u) + pheromone);
            }
            // Add pheromone to the last edge
            int first = tabu[0];
            int last = tabu[tabu.length - 1];
            trails.set(first, last, trails.get(first, last) + pheromone);
            trails.set(last, first, trails.get(last, first) + pheromone);
        }
    }

    private void initTrails() {
        log.debug("Init trail matrix for {} nodes", numOfNodes);
        trails = new Matrix(numOfNodes);
        for (int i = 0; i < numOfNodes; i++) {
            for (int j = i + 1; j < numOfNodes; j++) {
                trails.set(i, j, 1);
                trails.set(j, i, 1);
            }
        }
    }

    private void initAnts() {
        int numOfAnts = numOfNodes > 400 ? numOfNodes / 2 : numOfNodes;
        log.debug("Init {} ants", numOfAnts);
        ants = new Ant[numOfAnts];
        for (int i = 0, j = 0; i < numOfNodes && j < numOfAnts; i++, j++) {
            ants[j] = new Ant(i, numOfNodes);
        }
    }

    private boolean shouldContinue() {
        return numOfUselessCycles <= USELESS_CYCLES_THRESHOLD && curCycle++ < numOfCycles;
    }
}
