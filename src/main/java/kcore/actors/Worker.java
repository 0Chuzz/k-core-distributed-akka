package kcore.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import kcore.messages.*;
import kcore.structures.GraphWithCandidateSet;
import kcore.structures.GraphWithCoreness;

import java.util.HashMap;

/**
 * Worker actor for k-core calculation
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

    /**
     * Handles the addition of a processed frontier edge. Add a remote node to the graph and
     * update the coreness of the nodes.
     *
     * @param message
     */
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

    /**
     * Handle query for reachable nodes. builds the subgraph and returns it.
     *
     * @param message
     */
    private void handleReachableNodesQuery(ReachableNodesQuery message) {
        final ReachableNodesQuery query = message;
        int node = query.node;
        int coreness = graph.getCoreness(node);
        GraphWithCandidateSet reachSub = new GraphWithCandidateSet(graph, node);

        getSender().tell(new ReachableNodesReply(reachSub, partitionId, node, coreness), getSelf());
    }

    /**
     * Handles the request to load the partition data. Loads it from the specified source and
     * start calculating local coreness values.
     * @param message
     */
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

    /**
     * Handles query about node coreness. Gathers required data and returns it.
     * @param query
     */
    private void sendCorenessReply(CorenessQuery query) {
        HashMap<Integer, Integer> replymap = new HashMap<Integer, Integer>();
        for (int node : query.node1) {
            if (graph.contains(node))
                replymap.put(node, graph.getCoreness(node));

        }
        getSender().tell(new CorenessReply(replymap), getSelf());
    }


    /**
     * pretty print coreness values
     * @return pretty printed coreness
     */
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
