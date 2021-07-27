package com.outliers.algo.bnb;

import com.outliers.graph.Graph;
import com.outliers.graph.Matrix;

import java.util.List;

public class PrimsBasedMSTHeuristic {
    static int INT_MAX = Integer.MAX_VALUE;

    // gets MST by pre including a set of nodes.
    public static double getMSTLowerBound(Graph graph, List<Integer> nodesToPreInclude) {

        boolean[] inMST = new boolean[graph.getMatrix().getSize()];
        for (int node : nodesToPreInclude) {
            inMST[node] = true;
        }

        // Keep adding edges while number of included
        // edges does not become V-1.
        int edgeCount = nodesToPreInclude.size() - 1;
        double minCost = getMinCostFromNodesToInclude(nodesToPreInclude, graph);
        while (edgeCount < graph.getMatrix().getSize() - 1) {
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException("Timeout.");
            }
            // Find minimum weight valid edge.
            double min = INT_MAX;
            int a = -1, b = -1;
            for (int i = 0; i < graph.getMatrix().getSize(); i++) {
                if (!nodesToPreInclude.contains(i)) {
                    for (int j = 0; j < graph.getMatrix().getSize(); j++) {
                        if (graph.getMatrix().get(i,j) < min) {
                            if (isValidEdge(i, j, inMST)) {
                                min = graph.getMatrix().get(i,j);
                                a = i;
                                b = j;
                            }
                        }
                    }
                }
            }

            if (a != -1 && b != -1) {
                edgeCount++;
                minCost = minCost + min;
                inMST[b] = inMST[a] = true;

            }
        }
        return minCost;
    }

    private static double getMinCostFromNodesToInclude(List<Integer> nodesToPreInclude, Graph graph) {
        double pathCost = 0.0;
        for (int i = 0; i < nodesToPreInclude.size() - 1; i++) {
            pathCost += graph.getMatrix().get(nodesToPreInclude.get(i), nodesToPreInclude.get(i + 1));
        }
        return pathCost;
    }

    // Returns true if edge u-v is a valid edge to be
    // include in MST. An edge is valid if one end is
    // already included in MST and other is not in MST.
    static boolean isValidEdge(int u, int v,
                               boolean[] inMST) {
        if (u == v)
            return false;
        if (!inMST[u] && !inMST[v])
            return false;
        else return !inMST[u] || !inMST[v];
    }
}
