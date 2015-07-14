package kcore.messages;

import kcore.structures.GraphWithCandidateSet;

import java.io.Serializable;

/**
 * Reachability information
 */
public class ReachableNodesReply implements Serializable {
    public GraphWithCandidateSet graph;
    public int partition;
    public int node;
    public int coreness;


    public ReachableNodesReply(GraphWithCandidateSet reachableSubgraph, int partitionId, int node, int coreness) {

        graph = reachableSubgraph;
        partition = partitionId;
        this.node = node;
        this.coreness = coreness;
    }
}
