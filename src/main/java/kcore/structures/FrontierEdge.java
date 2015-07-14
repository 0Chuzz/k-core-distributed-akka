package kcore.structures;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Frontier edge data structure. This object represents a frontier edge inside the
 * master edge database.
 */
public class FrontierEdge {
    public int node1, node2;
    public int coreness1 = -1, coreness2 = -1;
    //public ActorRef worker1, worker2;
    public GraphWithCandidateSet subgraph;
    public HashSet<Integer> queryNodes;

    /**
     * Returns whether the edge is ready for the candidate set construction phase.
     *
     * @return
     */
    public boolean readyForCandidateSet() {
        return coreness1 != -1 && coreness2 != -1;
    }

    /**
     * Returns whether the edge is ready for the set pruning phase
     * @return
     */
    public boolean readyForPruning() {
        return queryNodes.isEmpty();
    }


    /**
     * Increment stored coreness value when a frontier edge is processed and this node still belongs to the
     * update set.
     * @param toBeUpdated
     */
    public void incrementLocalCoreness(HashSet<Integer> toBeUpdated) {
        if (toBeUpdated.contains(node1)) {
            coreness1++;
        }
        if (toBeUpdated.contains(node2)) {
            coreness2++;
        }
    }

    /**
     * Try to update reachable nodes and candidate set. Checks if new remote nodes have to be
     * queried.
     * @param node starting node
     * @param graph reachable subgraph
     * @return remote nodes to be queried
     */
    public HashSet<Integer> tryMergeGraph(int node, GraphWithCandidateSet graph) {
        if (queryNodes == null)
            initQueryNodes();
        if (queryNodes.contains(node)) {
            queryNodes.remove(node);
            subgraph.merge(graph);
            HashSet<Integer> ret = new HashSet<Integer>(subgraph.candidateRemotes);
            ret.removeAll(queryNodes);
            queryNodes.addAll(subgraph.candidateRemotes);
            return ret;
        }
        return null;
        //return subgraph.candidateRemotes;
    }

    /**
     * Initialize reachable subgraph and remote node query
     */
    public void initQueryNodes() {
        queryNodes = new HashSet<Integer>();
        subgraph = new GraphWithCandidateSet();
        subgraph.addEdge(node1, node2);
        HashMap<Integer, Integer> ctable = subgraph.getcorenessTable();
        ctable.put(node1, coreness1);
        ctable.put(node2, coreness2);
        if (coreness1 <= coreness2) {
            queryNodes.add(node1);
        }
        if (coreness2 <= coreness1) {
            queryNodes.add(node2);
        }
    }

    public int minCoreness() {
        return coreness1 < coreness2 ? coreness1 : coreness2;
    }

    /**
     * Unflag node that doesn't need to be queried for reachable nodes after all. (eg already in processed
     * edges)
     * @param n         node
     * @param coreness1 coreness of the node
     */
    public void shortcutMerge(int n, int coreness1) {
        queryNodes.remove(n);
        if (coreness1 > minCoreness()) {
            subgraph.getcorenessTable().put(n, coreness1);
        }
    }
}
