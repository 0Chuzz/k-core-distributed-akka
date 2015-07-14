package kcore.messages;

import kcore.structures.GraphWithCandidateSet;

import java.io.Serializable;

/**
 * Reachability information
 */
public class ReachableNodesReply implements Serializable {
    public final GraphWithCandidateSet graph;
    public final int partition, node, coreness;


    public ReachableNodesReply(GraphWithCandidateSet reachableSubgraph, int partitionId, int node, int coreness) {

        graph = reachableSubgraph;
        partition = partitionId;
        this.node = node;
        this.coreness = coreness;
    }
}
