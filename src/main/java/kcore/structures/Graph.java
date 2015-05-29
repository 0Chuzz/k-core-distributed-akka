package kcore.structures;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Graph implements Serializable {
    protected HashSet<Integer> nodes;
    protected HashMap<Integer, HashSet<Integer>> edges;

    public Graph() {
        nodes = new HashSet<Integer>();
        edges = new HashMap<Integer, HashSet<Integer>>();
    }

    public void addNode(int node) {
        assert !nodes.contains(node);
        nodes.add(node);
        edges.put(node, new HashSet<Integer>());
    }

    public void addEdge(int node1, int node2) {
        addNode(node1);
        addNode(node2);
        edges.get(node1).add(node2);
        edges.get(node2).add(node1);
    }

    public HashSet<Integer> getNeighbours(int node) {
        return edges.get(node);
    }

    public void merge(Graph g) {
        nodes.addAll(g.nodes);
        for (Map.Entry<Integer, HashSet<Integer>> entry : g.edges.entrySet()) {
            if (edges.containsKey(entry.getKey())) {
                edges.get(entry.getKey()).addAll(entry.getValue());
            } else {
                edges.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public boolean contains(int node) {
        return nodes.contains(node);
    }

    public HashSet<Integer> getNodes() {
        return nodes;
    }
}
