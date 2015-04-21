package kcore.messages;

import java.io.Serializable;

/**
 * Created by chuzz on 4/21/15.
 */
public class CorenessReply implements Serializable {
    public int node, partition, coreness;

    public CorenessReply(int node1, int coreness, int partition) {
        node = node1;
        this.coreness = coreness;
        this.partition = partition;
    }
}
