package kcore.structures;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by chuzz on 5/1/15.
 */
class EdgesMap extends HashMap<Integer, HashSet<Integer>> {
    @Override
    public HashSet<Integer> get(Object k) {
        if (!containsKey(k)) {
            put((Integer) k, new HashSet<Integer>());
        }
        return super.get(k);

    }
}
