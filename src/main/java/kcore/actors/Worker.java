package kcore.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import kcore.DistKCore;
import kcore.IntGraph;
import kcore.KShellFS;
import kcore.messages.*;

import java.io.File;
import java.util.HashSet;

/**
 * Created by Stefano on 09/03/2015.
 */
public class Worker extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    int[] corenessTable;
    IntGraph graph;
    int partitionId;

    @Override
    public void preStart() {
        log.debug("Worker starting");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.debug(message.toString());
        if (message instanceof LoadPartition) {
            final LoadPartition msg = (LoadPartition) message;
            partitionId = msg.getPartitionId();
            final String graphFile = "graphFile" + Integer.toString(msg.getPartitionId());

            graph = msg.getPartition();
            //transform the graph object IntGraph to a binary file
            DistKCore.graphFileConstruction(graph, graphFile);


            //step 1: execution of the standard k-core decomposition algorithm in parallel
            //System.out.println("****** Step 1: Computing the coreness in local partitions ******");
            KShellFS ks = new KShellFS();

            try {
                corenessTable = ks.execute(graphFile);
            } finally {
                new File(graphFile).delete();
            }
            log.info("partial coreness" + corenessToString());
            CorenessState corenessState;
            corenessState = new CorenessState(msg.getPartitionId());
            getSender().tell(corenessState, getSelf());

        } else if (message instanceof CorenessQuery) {
            final CorenessQuery query = (CorenessQuery) message;
            getSender().tell(new CorenessReply(query.node1, corenessTable[query.node1], partitionId), getSelf());
            //log.info("Coreness query for node {}", query.node1);
            // log.info("I am {}  and have a frontier edge from {} to {}", partitionId, fe.node1, 123);
        } else if (message instanceof ReachableNodesQuery) {
            final ReachableNodesQuery query = (ReachableNodesQuery) message;
            getSender().tell(new ReachableNodesReply(getReachableNodes(query.node, query.coreness), partitionId,
                    query.node, query.coreness), getSelf());
        }
    }


    public String corenessToString() {
        StringBuilder b = new StringBuilder();
        b.append("[");
        for (int i : corenessTable) {
            b.append(i);
            b.append(", ");
        }
        b.append("]");
        return b.toString();
    }

    public HashSet<Integer> getReachableNodes(int node, int coreness) {
        HashSet<Integer> ret = new HashSet<Integer>();
        getReachableNodes(node, coreness, ret);
        return ret;
    }

    public void getReachableNodes(int node, int coreness, HashSet<Integer> ret) {
        if (corenessTable[node] == coreness && !ret.contains(node)) {
            ret.add(node);
            for (int neighNode : graph.neighbors(node).getA()) {
                getReachableNodes(neighNode, coreness, ret);
            }
        }
    }

}
