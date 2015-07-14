package kcore.structures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Graph with remote nodes, as is nodes belonging to different partitions.
 */
public class GraphWithRemoteNodes extends Graph {
    protected HashSet<Integer> remoteNodes;
    protected HashMap<Integer, HashSet<Integer>> remoteEdges;

    public GraphWithRemoteNodes() {
        super();
        remoteNodes = new HashSet<Integer>();
        remoteEdges = new HashMap<Integer, HashSet<Integer>>();
    }


    public void addRemoteEdge(int localNode, int remoteNode) {
        remoteNodes.add(remoteNode);
        if (!remoteEdges.containsKey(localNode))
            remoteEdges.put(localNode, new HashSet<Integer>());
        remoteEdges.get(localNode).add(remoteNode);
    }


    public boolean isRemote(int node) {
        return remoteNodes.contains(node);
    }

    public HashSet<Integer> getNeighbours(int node) {

        if (remoteEdges.containsKey(node)) {
            HashSet<Integer> ret = new HashSet<Integer>();
            ret.addAll(super.getNeighbours(node));
            ret.addAll(remoteEdges.get(node));
            return ret;
        } else
            return super.getNeighbours(node);

    }

    public void merge(GraphWithRemoteNodes g) {
        super.merge(g);
        remoteNodes.addAll(g.remoteNodes);
        remoteNodes.removeAll(nodes);
        HashMap<Integer, HashSet<Integer>> oldremote = remoteEdges;
        remoteEdges = new HashMap<Integer, HashSet<Integer>>();
        for (Map.Entry<Integer, HashSet<Integer>> e : oldremote.entrySet()) {
            for (int n : e.getValue()) {
                if (nodes.contains(n)) {
                    addEdge(e.getKey(), n);
                } else {
                    addRemoteEdge(e.getKey(), n);
                }
            }
        }
        for (Map.Entry<Integer, HashSet<Integer>> e : g.getRemoteEdges().entrySet()) {
            for (int n : e.getValue()) {
                if (nodes.contains(n)) {
                    addEdge(e.getKey(), n);
                } else {
                    addRemoteEdge(e.getKey(), n);
                }
            }
        }
    }

    public HashSet<Integer> getRemoteNodes() {
        return remoteNodes;
    }

    public HashMap<Integer, HashSet<Integer>> getRemoteEdges() {
        return remoteEdges;
    }
}
