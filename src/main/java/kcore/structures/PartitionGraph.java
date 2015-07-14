package kcore.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Graph of the partitions. each edge holds the list of frontier edges between two partitions
 */
class PartitionGraph extends Graph {
    HashMap<Edge, ArrayList<FrontierEdge>> edgesList;
    HashMap<Integer, Integer> nodeToPartition;

    public PartitionGraph(HashMap<Integer, Integer> nToP) {
        edgesList = new HashMap<Edge, ArrayList<FrontierEdge>>();
        nodeToPartition = nToP;
    }

    public void addFrontierEdge(FrontierEdge fe) {
        int part1 = nodeToPartition.get(fe.node1);
        int part2 = nodeToPartition.get(fe.node2);
        Edge edge = new Edge(part1, part2);
        addEdge(part1, part2);
        if (!edgesList.containsKey(edge)) {
            edgesList.put(edge, new ArrayList<FrontierEdge>());
        }
        edgesList.get(edge).add(fe);
    }

    public FrontierEdgeTree getMergeTree() {
        HashMap<Integer, FrontierEdgeTree> partitionToTree = new HashMap<Integer, FrontierEdgeTree>();
        ArrayList<Edge> sortedMerges = new ArrayList<Edge>(edgesList.keySet());

        // heuristic merge order
        //sort from slowest to quickest
        Collections.sort(sortedMerges, new Comparator<Edge>() {
            @Override
            public int compare(Edge edge, Edge t1) {
                return -(edgesList.get(edge).size() - edgesList.get(t1).size());
            }
        });


        FrontierEdgeTree ret = null;

        for (Edge edge : sortedMerges) {
            FrontierEdgeTree left = null;
            FrontierEdgeTree right = null;
            if (partitionToTree.containsKey(edge.node1)) {
                left = partitionToTree.get(edge.node1);
            }
            if (partitionToTree.containsKey(edge.node2)) {
                right = partitionToTree.get(edge.node2);
            }

            if (left != right || left == null) {
                ret = new FrontierEdgeTree();
                ret.addEdges(edgesList.get(edge));
                ret.subTree1 = left;
                ret.subTree2 = right;
                partitionToTree.put(edge.node1, ret);
                partitionToTree.put(edge.node2, ret);
            } else {
                left.addEdges(edgesList.get(edge));
                ret = left;
            }
        }
        return ret;
    }
}
