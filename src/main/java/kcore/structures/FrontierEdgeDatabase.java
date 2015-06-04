package kcore.structures;

import java.util.*;

/**
 * Created by chuzz on 5/29/15.
 */
public class FrontierEdgeDatabase {
    public ArrayList<FrontierEdge> list = new ArrayList<FrontierEdge>();
    FrontierEdgeTree mergeTree;
    HashSet<FrontierEdge> mergableEdges = new HashSet<FrontierEdge>();
    HashSet<FrontierEdge> prunableEdges = new HashSet<FrontierEdge>();
    HashSet<FrontierEdge> processedEdges = new HashSet<FrontierEdge>();

    public void initMergeTree(HashMap<Integer, Integer> nToP) {
        PartitionGraph pg = new PartitionGraph(nToP);
        for (FrontierEdge fe : list) {
            pg.addFrontierEdge(fe);
        }
        mergeTree = pg.getMergeTree();
        mergableEdges.addAll(mergeTree.getReady());
    }


    public FrontierEdge get(int index) {
        return list.get(index);
    }

    public HashSet<Integer> mergeGraph(int node, GraphWithCandidateSet graph) {
        HashSet<Integer> retTot = new HashSet<Integer>();
        for (FrontierEdge db : mergableEdges) {
            HashSet<Integer> ret = db.tryMergeGraph(node, graph);
            if (ret == null) return ret;
            Iterator<Integer> it = ret.iterator();
            int n;
            while (it.hasNext()) {
                n = it.next();
                for (FrontierEdge fe : processedEdges) {
                    if (fe.node1 == n && fe.coreness1 != db.minCoreness()) {
                        db.shortcutMerge(n, fe.coreness1);
                        it.remove();
                        break;
                    }
                    if (fe.node2 == n && fe.coreness2 != db.minCoreness()) {
                        db.shortcutMerge(n, fe.coreness2);
                        it.remove();
                        break;
                    }
                }

            }
            retTot.addAll(ret);
            if (db.readyForPruning()) {
                prunableEdges.add(db);
            }
        }
        mergableEdges.removeAll(prunableEdges);
        return retTot;
    }

    public Collection<FrontierEdge> readyForPruning() {
        return prunableEdges;
    }

    public void incrementLocalCoreness(HashSet<Integer> toBeUpdated) {
        for (FrontierEdge db2 : list) {
            db2.incrementLocalCoreness(toBeUpdated);
        }
    }

    public void updateCorenessTable(HashMap<Integer, Integer> map) {
        for (FrontierEdge db : list) {
            if (map.containsKey(db.node1)) {
                db.coreness1 = map.get(db.node1);
                //db.worker1 = sender;
            }
            if (map.containsKey(db.node2)) {
                db.coreness2 = map.get(db.node2);
                //db.worker2 = sender;
            }
        }
    }

    public boolean processedEverything() {
        return list.size() == processedEdges.size();
    }

    public Collection<FrontierEdge> readyForCandidateSet() {
        return mergableEdges;
    }

    public void markCompleted(FrontierEdge db) {
        prunableEdges.remove(db);
        processedEdges.add(db);
        mergeTree.done(db);
        mergableEdges.addAll(mergeTree.getReady());
    }

    public ArrayList<FrontierEdge> getList() {
        return list;
    }

    public void add(FrontierEdge e) {
        this.list.add(e);
    }
}
