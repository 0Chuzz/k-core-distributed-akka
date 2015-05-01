package kcore.structures;

import java.util.HashMap;

/**
 * Created by chuzz on 5/1/15.
 */
class CorenessMap extends HashMap<Integer, Integer> {
    @Override
    public Integer get(Object key) {
        if (containsKey(key))
            return super.get(key);
        else
            return 0;
    }
}
