package kcore.structures;

import java.util.HashSet;

/**
 * Created by chuzz on 4/30/15.
 */
public class GraphWithCandidateSet extends GraphWithCoreness {
    protected HashSet<Integer> candidateSet;
    protected HashSet<Integer> candidateRemotes;
    protected HashSet<Integer> toBeUpdated = null;

    public GraphWithCandidateSet() {
        super();
        candidateSet = new HashSet<Integer>();
        candidateRemotes = new HashSet<Integer>();
    }


    public GraphWithCandidateSet(GraphWithCoreness graph, int node) {
        super();
        candidateSet = new HashSet<Integer>();
        candidateRemotes = new HashSet<Integer>();
        assert !graph.isRemote(node);
        recursiveTraverse(graph, node);
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

    private void recursiveTraverse(GraphWithCoreness g, int node) {
        if (candidateSet.contains(node)) return;
        candidateSet.add(node);
        for (int neighNode : g.getNeighbours(node)) {
            if (g.isRemote(neighNode)) {
                candidateRemotes.add(neighNode);
                addRemoteEdge(node, neighNode);
            } else {
                addEdge(node, neighNode);
                if (g.getCoreness(neighNode) == g.getCoreness(node)) {
                    recursiveTraverse(g, neighNode);
                }
            }
        }
    }

    public HashSet<Integer> getCandidateSet() {
        return candidateSet;
    }

    public HashSet<Integer> getRemoteCandidates() {
        return candidateRemotes;
    }

    public void merge(GraphWithCandidateSet graph) {
        super.merge(graph);
        this.candidateSet.addAll(graph.candidateSet);
        this.candidateRemotes.addAll(graph.candidateRemotes);
        this.candidateRemotes.removeAll(candidateSet);
    }

    public boolean waitingForRemote(int node) {
        return candidateRemotes.contains(node);
    }

    public boolean waitingForRemote() {
        return candidateRemotes.size() > 0;
    }

    public HashSet<Integer> getPrunedSet() {
        if (toBeUpdated == null) {
            HashSet<Integer> oldCandidateSet = new HashSet<Integer>(candidateSet);
            pruneCandidateNodes();
            toBeUpdated = candidateSet;
            candidateSet = oldCandidateSet;
        }
        return toBeUpdated;
    }
}
