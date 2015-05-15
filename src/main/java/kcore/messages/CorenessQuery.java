package kcore.messages;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chuzz on 4/21/15.
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
