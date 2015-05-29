package kcore.structures;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by chuzz on 5/22/15.
 */
public class GraphWithRemoteNodes extends Graph {
    protected HashSet<Integer> remoteNodes;
    protected HashMap<Integer, HashSet<Integer>> remoteEdges;

    GraphWithRemoteNodes() {
        super();
        remoteNodes = new HashSet<Integer>();
        remoteEdges = new HashMap<Integer, HashSet<Integer>>();
    }


    public void addRemoteEdge(int localNode, int remoteNode) {
        remoteNodes.add(remoteNode);
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
        remoteEdges.putAll(g.remoteEdges);
        for (HashSet<Integer> edges : remoteEdges.values()) {
            edges.removeAll(nodes);
        }
    }
}
