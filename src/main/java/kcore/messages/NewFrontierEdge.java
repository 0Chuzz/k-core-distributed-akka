package kcore.messages;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by chuzz on 4/16/15.
 */
public class NewFrontierEdge implements Serializable {
    public int node1, node2;
    public int coreness1, coreness2;
    public HashSet<Integer> toBeUpdated;

    public NewFrontierEdge(int node1, int node2, int coreness1, int coreness2, HashSet<Integer> candidateSet) {
        this.node1 = node1;
        this.node2 = node2;
        this.coreness1 = coreness1;
        this.coreness2 = coreness2;
        this.toBeUpdated = candidateSet;
    }
}
