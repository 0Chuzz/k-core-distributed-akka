package kcore.structures;

import java.util.ArrayList;

/**
 * Merge tree strategy, each node holds the list of edges to be processed and two children representing
 * previous merges.
 */
public class FrontierEdgeTree {
    ArrayList<FrontierEdge> edges = new ArrayList<FrontierEdge>();
    FrontierEdgeTree subTree1 = null, subTree2 = null;

    /**
     * Returns wheter a tree has no frontier edges
     *
     * @param t
     * @return
     */
    private static boolean empty(FrontierEdgeTree t) {
        return t == null || t.empty();
    }

    boolean empty() {
        return edges.size() == 0 && empty(subTree1) && empty(subTree2);
    }

    /**
     * Pull from the tree leafs all the edges that can be processed.
     * @return
     */
    public ArrayList<FrontierEdge> getReady() {
        ArrayList<FrontierEdge> ret = new ArrayList<FrontierEdge>();
        if (empty(subTree1) && empty(subTree2)) {
            if (!empty()) {
                ret.add(edges.get(0));
            }
        } else {
            if (subTree1 != null) ret.addAll(subTree1.getReady());
            if (subTree2 != null) ret.addAll(subTree2.getReady());
        }
        return ret;
    }

    /**
     * Add the edges to the list.
     * @param e
     */
    public void addEdges(ArrayList<FrontierEdge> e) {
        edges.addAll(e);
    }

    /**
     * returns wheter all edges have been processed.
     * @param e
     */
    public void done(FrontierEdge e) {
        if (!empty(subTree1)) {
            subTree1.done(e);
        }
        if (!empty(subTree2)) {
            subTree2.done(e);
        }
        if (edges.contains(e)) {
            edges.remove(e);
        }
    }
}
