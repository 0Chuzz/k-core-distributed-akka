package kcore.messages;

import java.io.Serializable;

/**
 * Created by chuzz on 4/21/15.
 */
public class CorenessQuery implements Serializable {
    public int node1;

    public CorenessQuery(int node1) {
        this.node1 = node1;
    }
}
