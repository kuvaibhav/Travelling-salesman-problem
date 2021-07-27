package com.outliers.algo.bnb;

import com.outliers.algo.Algorithm;
import com.outliers.graph.Graph;
import com.outliers.main.TSP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.outliers.algo.bnb.PrimsBasedMSTHeuristic.getMSTLowerBound;

public class BranchAndBound implements Algorithm {
    private final Graph graph;
    private int start;
    private double upperLimit = Integer.MAX_VALUE;
    private int noOfExpansions = 0;
    private static final Logger log = LogManager.getLogger(TSP.class);
    int count = 0;

    public BranchAndBound(Graph graph, int start) {
        this.graph = graph;
        this.start = start;
    }

    @Override
    public void run() {
        if (start >= graph.getMatrix().getSize()) {
            String message = "Invalid starting node. Please enter a starting node from (0 to size-1)";
            log.error(message);
            return;
        }
        List<Integer> nodesWithoutStart = new ArrayList<>();
        for (int i = 0; i < graph.getMatrix().getSize(); i++) {
            if (i != start) {
                nodesWithoutStart.add(i);
            }
        }
        branchAndBound(nodesWithoutStart, "", 0.0);
    }

    @Override
    public List<String> getValues() {
        // TODO
        return new ArrayList<String>(Arrays.asList(String.valueOf(upperLimit), String.valueOf(noOfExpansions)));
    }

    private void branchAndBound(List<Integer> nodeListWithoutStart, String soFar, double mc) {
        String[] nodesInSoFar = soFar.split(",");
        if (nodesInSoFar.length == nodeListWithoutStart.size()) {
            count++;
            String tsp_path = getTspPath(nodesInSoFar);
            double tspCost = getTspCost(mc, nodesInSoFar);
            if (tspCost < upperLimit) {
                upperLimit = tspCost;
                log.debug("Best TSP path found so far: " + tsp_path + " Cost:" + tspCost);
            }
            return;
        }
        for (int i : nodeListWithoutStart) {
            if (hasString(nodesInSoFar, "" + i)) {
                List<Integer> nodesToPreInclude = getNodesToInclude(new ArrayList<>(Arrays.asList(nodesInSoFar)));
                nodesToPreInclude.add(i);
                nodesToPreInclude.add(0, start);
                double minCost = getMSTLowerBound(graph, nodesToPreInclude);
                if (minCost < upperLimit) {
                    noOfExpansions++;
                    branchAndBound(nodeListWithoutStart, soFar + i + ",", minCost);
                }
                else{
                    log.debug("Pruning condition satisfied");
                }
            }
        }
    }

    private double getTspCost(double mc, String[] nodesInSoFar) {
        double tspCost;
        tspCost = mc + this.graph.getMatrix().get(Integer.parseInt(nodesInSoFar[nodesInSoFar.length - 1]), start);
        return tspCost;
    }

    private String getTspPath(String[] nodesInSoFar) {
        StringBuilder tspPath = new StringBuilder();
        tspPath.append(start).append("->");
        for (String s : nodesInSoFar) {
            tspPath.append(s).append("->");
        }
        tspPath.append(start);
        return tspPath.toString();
    }

    private List<Integer> getNodesToInclude(ArrayList<String> strings) {
        List<Integer> nti = new ArrayList<>();
        for (String s : strings) {
            if (!s.equals("")) {
                nti.add(Integer.parseInt(s));
            }
        }
        return nti;
    }

    private boolean hasString(String[] nodesInSoFar, String s) {
        for (String st : nodesInSoFar) {
            if (st.equals(s)) {
                return false;
            }
        }
        return true;
    }
}
