package kcore.messages;

import java.io.Serializable;

/**
 * Query reachability information
 */
public class ReachableNodesQuery implements Serializable {
    public int node;
    //public int coreness;

    public ReachableNodesQuery(int node) {
        this.node = node;
        //coreness = coreness1;
    }
}
