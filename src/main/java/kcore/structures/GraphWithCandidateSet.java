package kcore.structures;

import java.util.HashSet;

/**
 * Graph with remote nodes, coreness values and candidate set.
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

    /**
     * Build a new graph, containing the reachable nodes from a starting node having the same
     * coreness values, plus neighbours having higher coreness.
     *
     * @param graph
     * @param node
     */
    public GraphWithCandidateSet(GraphWithCoreness graph, int node) {
        super();
        candidateSet = new HashSet<Integer>();
        candidateRemotes = new HashSet<Integer>();
        if (graph.isRemote(node)) throw new RuntimeException();
        recursiveTraverse(graph, node);
        updateCorenessFrom(graph);
    }


    /**
     * prune the candidate nodes by counting neighbours
     */
    public void pruneCandidateNodes() {
        boolean changed = false;
        HashSet<Integer> removed = new HashSet<Integer>();
        for (int node : candidateSet) {
            int count = 0;
            for (int neighbour : getNeighbours(node)) {
                if (candidateSet.contains(neighbour) || corenessTable.containsKey(neighbour) &&
                        corenessTable.get(neighbour) > corenessTable.get(node)) {
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

    /**
     * Recursively create the graph by checking every neighbour.
     * @param g source graph
     * @param node analyzed node
     */
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
        return new HashSet<Integer>(candidateSet);
    }

    public HashSet<Integer> getRemoteCandidates() {
        return candidateRemotes;
    }

    /**
     * merge a subgraph from a remote partition
     * @param graph
     */
    public void merge(GraphWithCandidateSet graph) {
        super.merge(graph);
        this.candidateSet.addAll(graph.candidateSet);
        this.candidateRemotes.addAll(graph.candidateRemotes);
        this.candidateRemotes.removeAll(candidateSet);
    }

    /**
     * Checks whether we need to query a remote node.
     * @param node
     * @return
     */
    public boolean waitingForRemote(int node) {
        return candidateRemotes.contains(node);
    }

    /**
     * Checks whether we need to query more partitions, as the reachability graph
     * contains remote nodes.
     * @return
     */
    public boolean waitingForRemote() {
        return candidateRemotes.size() > 0;
    }

    /**
     * Get the set of nodes to be updates
     * @return
     */
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
