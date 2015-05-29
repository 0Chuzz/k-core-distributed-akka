package kcore.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import kcore.messages.*;
import kcore.structures.GraphWithCandidateSet;
import kcore.structures.GraphWithCoreness;

import java.util.HashMap;

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
    public void postStop() {
        log.info("final coreness: {}", corenessToString());
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
        int localEdge, remoteEdge, remoteCoreness;
        if (graph.contains(frontierEdge.node1)) {
            localEdge = frontierEdge.node1;
            remoteEdge = frontierEdge.node2;
            remoteCoreness = frontierEdge.coreness2;
        } else {
            localEdge = frontierEdge.node2;
            remoteEdge = frontierEdge.node1;
            remoteCoreness = frontierEdge.coreness1;
        }
        graph.addRemoteEdge(localEdge, remoteEdge, remoteCoreness);
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
            if (graph.contains(node))
                replymap.put(node, graph.getCoreness(node));

        }
        getSender().tell(new CorenessReply(replymap), getSelf());
    }


    public String corenessToString() {
        StringBuilder b = new StringBuilder();
        b.append("[");
        for (int i : graph.getNodes()) {
            b.append(Integer.toString(i) + "=" + graph.getCoreness(i));
            b.append(", ");
        }
        b.append("]");
        return b.toString();
    }



}
