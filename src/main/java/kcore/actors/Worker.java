package kcore.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import kcore.messages.*;
import kcore.structures.GraphWithCandidateSet;
import kcore.structures.GraphWithCoreness;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stefano on 09/03/2015.
 */

public class Worker extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    //int[] corenessTable;
    GraphWithCoreness graph;
    int partitionId;
    //ArrayList<FrontierEdgeDb> frontierEdges;
    //HashSet<Integer> partitionCandidateNodes;

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
            //final String graphFile = "graphFile" + Integer.toString(msg.getPartitionId());
            graph = new GraphWithCoreness(msg.getPartition());


            log.info("partial coreness" + corenessToString());
            CorenessState corenessState;
            corenessState = new CorenessState(msg.getPartitionId());
            getSender().tell(corenessState, getSelf());

        } else if (message instanceof CorenessQuery) {
            final CorenessQuery query = (CorenessQuery) message;
            sendCorenessReply(query);

        } else if (message instanceof ReachableNodesQuery) {
            final ReachableNodesQuery query = (ReachableNodesQuery) message;
            int node = query.node;
            int coreness = graph.getCoreness(node);
            GraphWithCandidateSet reachSub = new GraphWithCandidateSet(graph, node);

            getSender().tell(new ReachableNodesReply(reachSub, partitionId, node, coreness), getSelf());
        }
    }

    private void sendCorenessReply(CorenessQuery query) {
        HashMap<Integer, Integer> replymap = new HashMap<Integer, Integer>();
        for (int node : query.node1) {
            replymap.put(node, graph.getCoreness(node));

        }
        getSender().tell(new CorenessReply(replymap, partitionId), getSelf());
    }


    public String corenessToString() {
        StringBuilder b = new StringBuilder();
        b.append("[");
        for (Map.Entry<Integer, Integer> i : graph.getcorenessTable().entrySet()) {
            b.append(i.getKey().toString() + "=" + i.getValue());
            b.append(", ");
        }
        b.append("]");
        return b.toString();
    }



}
