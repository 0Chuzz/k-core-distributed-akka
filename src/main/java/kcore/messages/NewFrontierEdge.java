package kcore.messages;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by chuzz on 4/16/15.
 */
public class NewFrontierEdge implements Serializable {
    public int node1, node2;
    public HashSet<Integer> toBeUpdated;

    public NewFrontierEdge(int node1, int node2, HashSet<Integer> candidateSet) {
        this.node1 = node1;
        this.node2 = node2;
        this.toBeUpdated = candidateSet;
    }
}
