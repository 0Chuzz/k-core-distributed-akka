package kcore.structures;

import java.util.*;

/**
 * Database of frontier edges. Master handles the frontier edge processing
 * status through this class.
 */
public class FrontierEdgeDatabase {
    public ArrayList<FrontierEdge> list = new ArrayList<FrontierEdge>();
    FrontierEdgeTree mergeTree;
    HashSet<FrontierEdge> mergableEdges = new HashSet<FrontierEdge>();
    HashSet<FrontierEdge> prunableEdges = new HashSet<FrontierEdge>();
    HashSet<FrontierEdge> processedEdges = new HashSet<FrontierEdge>();

    /**
     * Initialize merge tree strategy.
     *
     * @param nToP node to partition map.
     */
    public void initMergeTree(HashMap<Integer, Integer> nToP) {
        PartitionGraph pg = new PartitionGraph(nToP);
        for (FrontierEdge fe : list) {
            pg.addFrontierEdge(fe);
        }
        mergeTree = pg.getMergeTree();
        mergableEdges.addAll(mergeTree.getReady());
    }


    /**
     * Get a frontier edge from the list.
     * @param index
     * @return
     */
    public FrontierEdge get(int index) {
        return list.get(index);
    }

    /**
     * Merge a reachability subgraph into the appropriate frontier edge entry.
     * If this entry has the full reachable graph, move it into the prunable set, else
     * return to Master the list of new nodes to query.
     */
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

    /**
     * Return all the edges that are ready for the pruning phase.
     * @return
     */
    public Collection<FrontierEdge> readyForPruning() {
        return prunableEdges;
    }

    /**
     * Increment local stored coreness values from updated nodes set.
     * @param toBeUpdated
     */
    public void incrementLocalCoreness(HashSet<Integer> toBeUpdated) {
        for (FrontierEdge db2 : list) {
            db2.incrementLocalCoreness(toBeUpdated);
        }
    }

    /**
     * Update local coreness value from coreness map received from worker
     * @param map node to coreness map
     */
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

    /**
     * Returns whether every node has been completely processed.
     * @return
     */
    public boolean processedEverything() {
        return list.size() == processedEdges.size();
    }

    /**
     * Returns the set of edges that are ready for the candidate set phase
     * @return
     */
    public Collection<FrontierEdge> readyForCandidateSet() {
        return mergableEdges;
    }

    /**
     * Move a frontier edge into the complete list, and update merge tree.
     * @param db
     */
    public void markCompleted(FrontierEdge db) {
        prunableEdges.remove(db);
        processedEdges.add(db);
        mergeTree.done(db);
        mergableEdges.addAll(mergeTree.getReady());
    }

    /**
     * Get list of frontier edges
     * @return
     */
    public ArrayList<FrontierEdge> getList() {
        return list;
    }

    /**
     * Add a frontier edge
     * @param e
     */
    public void add(FrontierEdge e) {
        this.list.add(e);
    }
}
