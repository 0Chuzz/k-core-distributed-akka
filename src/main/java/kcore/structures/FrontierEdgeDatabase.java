package kcore.structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by chuzz on 5/29/15.
 */
public class FrontierEdgeDatabase extends ArrayList<FrontierEdge> {
    public HashSet<Integer> mergeGraph(int node, GraphWithCandidateSet graph) {
        FrontierEdge db = get(0);
        return db.tryMergeGraph(node, graph);
    }

    public Collection<FrontierEdge> readyForPruning() {
        ArrayList<FrontierEdge> ret = new ArrayList<FrontierEdge>();
        if (get(0).readyForPruning())
            ret.add(get(0));
        return ret;
    }

    public void incrementLocalCoreness(HashSet<Integer> toBeUpdated) {
        for (FrontierEdge db2 : this) {
            db2.incrementLocalCoreness(toBeUpdated);
        }
    }

    public void updateCorenessTable(HashMap<Integer, Integer> map) {
        for (FrontierEdge db : this) {
            if (map.containsKey(db.node1)) {
                db.coreness1 = map.get(db.node1);
                //db.worker1 = sender;
            }
            if (map.containsKey(db.node2)) {
                db.coreness2 = map.get(db.node2);
                //db.worker2 = sender;
            }
            if (db.readyForCandidateSet()) {
                db.initQueryNodes();
            }
        }
    }

    public boolean processedEverything() {
        return this.size() > 0;
    }

    public Collection<FrontierEdge> readyForCandidateSet() {
        ArrayList<FrontierEdge> ret = new ArrayList<FrontierEdge>();
        if (get(0).readyForCandidateSet()) {
            ret.add(get(0));
        }
        return ret;
    }

    public void markCompleted(FrontierEdge db) {
        if (db != get(0)) throw new RuntimeException();
        remove(0);
    }
}
