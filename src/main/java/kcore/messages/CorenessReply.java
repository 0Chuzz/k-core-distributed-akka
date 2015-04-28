package kcore.messages;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by chuzz on 4/21/15.
 */
public class CorenessReply implements Serializable {
    //public int[] node, coreness;
    public HashMap<Integer, Integer> map;
    public int partition;

    /*public CorenessReply(int[] node1, int[] coreness, int partition) {
        node = node1;
        this.coreness = coreness;
        this.partition = partition;
    }*/

    public CorenessReply(HashMap<Integer, Integer> replymap, int partitionId) {
        map = replymap;
        partition = partitionId;
    }
}
