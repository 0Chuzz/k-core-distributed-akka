package kcore.structures;

import java.util.HashSet;

/**
 * Created by Stefano on 09/03/2015.
 */
public class FrontierEdge {
    public int node1, node2;
    public int coreness1 = -1, coreness2 = -1;
    //public ActorRef worker1, worker2;
    public GraphWithCandidateSet subgraph;
    public HashSet<Integer> queryNodes;

    public boolean readyForCandidateSet() {
        return coreness1 != -1 && coreness2 != -1;
    }

    public boolean readyForPruning() {
        return queryNodes.isEmpty();
    }


    public void incrementLocalCoreness(HashSet<Integer> toBeUpdated) {
        if (toBeUpdated.contains(node1)) {
            coreness1++;
        }
        if (toBeUpdated.contains(node2)) {
            coreness2++;
        }
    }

    public void tryMergeGraph(int node, GraphWithCandidateSet graph) {
        if (queryNodes.contains(node)) {
            queryNodes.remove(node);
            subgraph.merge(graph);
            queryNodes.addAll(subgraph.candidateRemotes);
        }
        //return subgraph.candidateRemotes;
    }

    public void initQueryNodes() {
        queryNodes = new HashSet<Integer>();
        subgraph = new GraphWithCandidateSet();
        if (coreness1 <= coreness2) {
            queryNodes.add(node1);
        }
        if (coreness2 <= coreness1) {
            queryNodes.add(node2);
        }
    }
}
