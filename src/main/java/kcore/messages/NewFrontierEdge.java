package kcore.messages;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by chuzz on 4/16/15.
 */
public class NewFrontierEdge implements Serializable {
    /**
     * nodes ids for the edge
     */
    public int node1, node2;
    /**
     * coreness of the two nodes
     */
    public int coreness1, coreness2;
    /**
     * set of nodes which coreness needs to be updated
     */
    public HashSet<Integer> toBeUpdated;

    public NewFrontierEdge(int node1, int node2, int coreness1, int coreness2, HashSet<Integer> candidateSet) {
        this.node1 = node1;
        this.node2 = node2;
        this.coreness1 = coreness1;
        this.coreness2 = coreness2;
        this.toBeUpdated = candidateSet;
    }
}
