package kcore.structures;

import java.util.*;

/**
 * Created by chuzz on 5/29/15.
 */
public class FrontierEdgeDatabase extends ArrayList<FrontierEdge> {
    int index = 0;
    public HashSet<Integer> mergeGraph(int node, GraphWithCandidateSet graph) {
        FrontierEdge db = get(index);
        HashSet<Integer> ret = db.tryMergeGraph(node, graph);
        if (ret == null) return ret;
        Iterator<Integer> it = ret.iterator();
        int n;
        while (it.hasNext()) {
            n = it.next();
            for (FrontierEdge fe : this) {
                if (fe.node1 == n && fe.coreness1 != db.minCoreness()) {
                    db.queryNodes.remove(n);
                    it.remove();
                    break;
                }
                if (fe.node2 == n && fe.coreness2 != db.minCoreness()) {
                    db.queryNodes.remove(n);
                    it.remove();
                    break;
                }
            }

        }
        return ret;
    }

    public Collection<FrontierEdge> readyForPruning() {
        ArrayList<FrontierEdge> ret = new ArrayList<FrontierEdge>();
        if (get(index).readyForPruning())
            ret.add(get(index));
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
        if (get(index).readyForCandidateSet()) {
            ret.add(get(index));
        }
        return ret;
    }

    public void markCompleted(FrontierEdge db) {
        if (db != get(index)) throw new RuntimeException();
        index++;
    }
}
