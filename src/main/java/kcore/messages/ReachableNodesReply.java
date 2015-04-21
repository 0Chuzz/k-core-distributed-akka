package kcore.messages;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by chuzz on 4/21/15.
 */
public class ReachableNodesReply implements Serializable {
    public final HashSet<Integer> reachableNodes;
    public final int partition, node, coreness;

    public ReachableNodesReply(HashSet<Integer> reachableNodes, int partition, int node, int coreness) {
        this.reachableNodes = reachableNodes;
        this.partition = partition;
        this.node = node;
        this.coreness = coreness;
    }
}
