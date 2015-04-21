package kcore.messages;

import java.io.Serializable;

/**
 * Created by chuzz on 4/21/15.
 */
public class ReachableNodesQuery implements Serializable {
    public int node;
    public int coreness;

    public ReachableNodesQuery(int node1, int coreness1) {
        node = node1;
        coreness = coreness1;
    }
}
