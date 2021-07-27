package com.outliers.algo.bnb;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class KrushkalSpanningTreeHeuristic {

}

class Edge {
    int source;
    int destination;
    int weight;

    public Edge(int source, int destination, int weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }
}

/**
 * Todo
 * Change below interface to integrate with Branch and Bound Class
 * Only mock data is used currently.
 */
class Graph {
    int vertices;
    ArrayList<Edge> allEdges = new ArrayList<>();

    Graph(int vertices) {
        this.vertices = vertices;
    }

    public void addEgde(int source, int destination, int weight) {
        Edge edge = new Edge(source, destination, weight);
        allEdges.add(edge); //add to total edges
    }

    public void kruskalMST(ArrayList<Edge> edges){
        PriorityQueue<Edge> pq = new PriorityQueue<>(allEdges.size()- edges.size(), Comparator.comparingInt(o -> o.weight));

        //add all the edges to priority queue, //sort the edges on cost
        for (int i = 0; i <allEdges.size(); i++) {
            boolean ignore = false;
            for(Edge edge: edges) {
                if (edge.source == allEdges.get(i).source && edge.destination == allEdges.get(i).destination) {
                    ignore = true;
                    break;
                }
            }
            if(!ignore) {
                pq.add(allEdges.get(i));
            }
        }

        int [] parent = new int[vertices];

        makeSet(parent);

        ArrayList<Edge> mst = new ArrayList<>();
        for (Edge edge: edges) {
            int x_set = find(parent, edge.source);
            int y_set = find(parent, edge.destination);
            union(parent, x_set, y_set);
        }

        int index = 0;
        while(index<(vertices-(1+edges.size()))){
            Edge edge = pq.remove();
            //check if adding this edge creates a cycle
            int x_set = find(parent, edge.source);
            int y_set = find(parent, edge.destination);

            if(x_set==y_set){
            }else {
                mst.add(edge);
                index++;
                union(parent,x_set,y_set);
            }
        }
        //print MST
        System.out.println("Minimum Spanning Tree: ");
        printGraph(mst);
    }

    public void makeSet(int [] parent){
        for (int i = 0; i <vertices ; i++) {
            parent[i] = i;
        }
    }

    public int find(int [] parent, int vertex){
        if(parent[vertex]!=vertex)
            return find(parent, parent[vertex]);;
        return vertex;
    }

    public void union(int [] parent, int x, int y){
        int x_set_parent = find(parent, x);
        int y_set_parent = find(parent, y);
        //make x as parent of y
        parent[y_set_parent] = x_set_parent;
    }

    /**
     *
     * @param edgeList Final MST created. The total MST value will be cost of all edge
     *                 from this list + the fixed edges.
     */
    public void printGraph(ArrayList<Edge> edgeList){
        for (int i = 0; i <edgeList.size() ; i++) {
            Edge edge = edgeList.get(i);
            /**
             * Todo: change sopln to logger.info()
             */
            System.out.println("Edge-" + i + " source: " + edge.source +
                " destination: " + edge.destination +
                " weight: " + edge.weight);
        }
    }
}