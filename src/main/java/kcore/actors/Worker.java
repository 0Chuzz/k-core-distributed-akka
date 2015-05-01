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
            handleLoadPartition((LoadPartition) message);

        } else if (message instanceof CorenessQuery) {
            sendCorenessReply((CorenessQuery) message);

        } else if (message instanceof ReachableNodesQuery) {
            handleReachableNodesQuery((ReachableNodesQuery) message);

        } else if (message instanceof NewFrontierEdge) {
            handleNewFrontierEdge((NewFrontierEdge) message);
        }
    }

    private void handleNewFrontierEdge(NewFrontierEdge message) {
        final NewFrontierEdge frontierEdge = message;
        graph.addEdge(frontierEdge.node1, frontierEdge.node2);
        graph.updateCorenessFrom(frontierEdge.toBeUpdated);
    }

    private void handleReachableNodesQuery(ReachableNodesQuery message) {
        final ReachableNodesQuery query = message;
        int node = query.node;
        int coreness = graph.getCoreness(node);
        GraphWithCandidateSet reachSub = new GraphWithCandidateSet(graph, node);

        getSender().tell(new ReachableNodesReply(reachSub, partitionId, node, coreness), getSelf());
    }

    private void handleLoadPartition(LoadPartition message) {
        final LoadPartition msg = message;
        partitionId = msg.getPartitionId();
        //final String graphFile = "graphFile" + Integer.toString(msg.getPartitionId());
        graph = new GraphWithCoreness(msg.getPartition());


        log.info("partial coreness" + corenessToString());
        CorenessState corenessState;
        corenessState = new CorenessState(msg.getPartitionId());
        getSender().tell(corenessState, getSelf());
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
