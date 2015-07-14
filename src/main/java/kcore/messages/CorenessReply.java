package kcore.messages;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Coreness values reply
 */
public class CorenessReply implements Serializable {
    //public int[] node, coreness;
    /**
     * map from node to respective coreness
     */
    public HashMap<Integer, Integer> map;

    public CorenessReply(HashMap<Integer, Integer> replymap) {
        map = replymap;
    }
}
