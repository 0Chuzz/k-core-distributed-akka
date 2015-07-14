package kcore.messages;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Query coreness values
 */
public class CorenessQuery implements Serializable {
    /**
     * list of the nodes which coreness is requested
     */
    public ArrayList<Integer> node1;

    public CorenessQuery(ArrayList<Integer> node1) {
        this.node1 = node1;
    }
}
