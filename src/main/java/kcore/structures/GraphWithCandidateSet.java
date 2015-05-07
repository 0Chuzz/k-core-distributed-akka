package kcore.structures;

import java.util.HashSet;

/**
 * Created by chuzz on 4/30/15.
 */
public class GraphWithCandidateSet extends GraphWithCoreness {
    protected HashSet<Integer> candidateSet;

    public GraphWithCandidateSet() {
        super();
        candidateSet = new HashSet<Integer>();
    }


    public GraphWithCandidateSet(GraphWithCoreness graph, int node) {
        super();
        candidateSet = new HashSet<Integer>();
        int coreness = graph.getCoreness(node);
        recursiveTraverse(graph, node, coreness);
        updateCorenessFrom(graph);
    }

    public void pruneCandidateNodes() {
        boolean changed = false;
        HashSet<Integer> removed = new HashSet<Integer>();
        for (int node : candidateSet) {
            int count = 0;
            for (int neighbour : getNeighbours(node)) {
                if (candidateSet.contains(neighbour) || corenessTable.get(neighbour) > corenessTable.get(node)) {
                    count++;
                }
            }
            if (count <= corenessTable.get(node)) {
                changed = true;
                removed.add(node);
            }
        }
        if (changed) {
            candidateSet.removeAll(removed);
            pruneCandidateNodes();
        }
    }

    private void recursiveTraverse(GraphWithCoreness g, int node, int coreness) {
        if (candidateSet.contains(node)) return;
        candidateSet.add(node);
        for (int neighNode : g.getNeighbours(node)) {
            addEdge(node, neighNode);
            if (g.getCoreness(neighNode) == coreness) {
                recursiveTraverse(g, neighNode, coreness);
            }
        }
    }

    public HashSet<Integer> getCandidateSet() {
        return candidateSet;
    }

    public void union(GraphWithCandidateSet graph) {
        this.merge(graph);
        this.corenessTable.putAll(graph.corenessTable);
        this.candidateSet.addAll(graph.candidateSet);
    }
}
