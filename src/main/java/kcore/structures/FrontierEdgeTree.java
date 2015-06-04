package kcore.structures;

import java.util.ArrayList;

public class FrontierEdgeTree {
    ArrayList<FrontierEdge> edges = new ArrayList<FrontierEdge>();
    FrontierEdgeTree subTree1 = null, subTree2 = null;

    private static boolean empty(FrontierEdgeTree t) {
        return t == null || t.empty();
    }

    boolean empty() {
        return edges.size() == 0 && empty(subTree1) && empty(subTree2);
    }

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

    public void addEdges(ArrayList<FrontierEdge> e) {
        edges.addAll(e);
    }

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
