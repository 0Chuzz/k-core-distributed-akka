package kcore.actors;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.dispatch.sysmsg.Terminate;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;
import com.typesafe.config.Config;
import kcore.messages.*;
import kcore.structures.FrontierEdge;
import kcore.structures.FrontierEdgeDatabase;
import kcore.structures.GraphWithCandidateSet;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Master actor for k-core calculation
 */

public class Master extends UntypedActor {
    private final Cancellable tick = getContext().system().scheduler().schedule(
            Duration.create(500, TimeUnit.MILLISECONDS),
            Duration.create(1, TimeUnit.SECONDS),
            getSelf(), "tick", getContext().dispatcher(), null);
    ActorRef backend = getContext().actorOf(FromConfig.getInstance().props(),
            "workersRouter");
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    HashMap<Integer, Integer> nodeToPartition = new HashMap<Integer, Integer>();
    HashMap<Integer, ActorRef> partitionToActor = new HashMap<Integer, ActorRef>();
    FrontierEdgeDatabase frontierEdges = new FrontierEdgeDatabase();
    HashMap<Integer, LoadPartition> partitionMsgs;
    int numPartitions, corenessReceived;

    @Override
    public void postStop() {
        tick.cancel();
        Cluster.get(getContext().system()).shutdown();
        getContext().system().shutdown();
        System.exit(0);
    }

    @Override
    public void preStart() {
        log.debug("Master starting");
        // start work
        Config conf = getContext().system().settings().config();
        int totalInstances = splitPartitionFiles(conf.getString("k-core.graph-file"),
                conf.getString("k-core.part-file"));
        numPartitions = totalInstances;
        corenessReceived = 0;
        partitionMsgs = new HashMap<Integer, LoadPartition>();
        for (int i = 0; i < totalInstances; i++) {
            partitionMsgs.put(i, new WholeLoadPartition("smallfile" + i, i));
        }

    }

    /**
     * Split a single graph file into multiple files, one for each partition
     *
     * @param graphfile name of the file containing the graph
     * @param partfile  name of the file containing the partition info
     * @return number of partitions
     */
    private int splitPartitionFiles(String graphfile, String partfile) {
        Scanner reader;

        try {
            reader = new Scanner(new File(partfile));
        } catch (FileNotFoundException e) {
            log.warning("file {} not found: loading example partfile", partfile);
            reader = new Scanner(getClass().getResourceAsStream("/partfile"));
        }

        Set<Integer> partitions = new HashSet<Integer>();
        FileWriter out;
        int node = 0;
        while (reader.hasNextInt()) {
            int partition = reader.nextInt();
            nodeToPartition.put(node, partition);
            partitions.add(partition);
            node++;
        }


        FileWriter[] partitionFiles = new FileWriter[partitions.size()];
        for (int p : partitions) {
            try {
                partitionFiles[p] = new FileWriter("smallfile" + (p));
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }

        try {
            reader = new Scanner(new File(graphfile));
        } catch (FileNotFoundException e) {
            log.warning("file {} not found: loading example graphfile", graphfile);
            reader = new Scanner(getClass().getResourceAsStream("/graphfile"));

        }
        while (reader.hasNextInt()) {
            int node1 = reader.nextInt();
            int node2 = reader.nextInt();
            if (nodeToPartition.get(node2) == nodeToPartition.get(node1)) {
                try {
                    partitionFiles[nodeToPartition.get(node1)].write("" + node1 + " " + node2 + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                FrontierEdge e = new FrontierEdge();
                e.node1 = node1;
                e.node2 = node2;
                frontierEdges.add(e);
            }
        }
        for (FileWriter w : partitionFiles) {
            try {
                w.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return partitions.size();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        // collect results
        log.debug(message.toString());

        if (message.equals("tick")) {
            for (LoadPartition msg : partitionMsgs.values()) {
                backend.tell(msg, getSelf());
            }
        } else if (message instanceof CorenessState) {
            handleCorenessState((CorenessState) message);

        } else if (message instanceof NewPartitionActor) {
            handleNewPartitionActor((NewPartitionActor) message);

        } else if (message instanceof CorenessReply) {
            handleCorenessReply((CorenessReply) message);

        } else if (message instanceof ReachableNodesReply) {
            handleReachableNodesReply((ReachableNodesReply) message);

        } else if (message instanceof FinalCorenessReply) {
            try {
                writeResult(((FinalCorenessReply) message).table);
            } catch (IOException e) {
                e.printStackTrace();
            }
            getSender().tell(new Terminate(), getSelf());
            corenessReceived++;
            log.info("coreness received: {}", corenessReceived);
            if (corenessReceived == numPartitions) {
                log.info("master exiting");
                Cluster cluster = Cluster.get(getContext().system());
                cluster.subscribe(getSelf(), ClusterEvent.MemberExited.class);

                cluster.leave(cluster.selfAddress());
            }
        } else if (message instanceof ClusterEvent.MemberExited) {
            //getContext().system().shutdown();
            getSelf().tell(new Terminate(), getSelf());
        }
    }

    /**
     * Handles the result of a reachable nodes query.
     * <p>
     * First it updates the frontier edge database. If some edge is ready for pruning phase, process it.
     *
     * @param message message to be handled
     */
    private void handleReachableNodesReply(ReachableNodesReply message) {
        log.info("got reachable nodes from node {}", message.node);

        HashSet<Integer> newFrontierEdges = frontierEdges.mergeGraph(message.node, message.graph);
        if (newFrontierEdges != null) for (int i : newFrontierEdges) {
            askReachableNodes(i);
        }

        for (FrontierEdge db : frontierEdges.readyForPruning()) {
            GraphWithCandidateSet unionSet = db.subgraph;
            HashSet<Integer> candidateSet = unionSet.getCandidateSet();
            HashSet<Integer> toBeUpdated = unionSet.getPrunedSet();
            log.info("frontier edge {}-{}: candidateSet: {} toBeUpdated: {}", db.node1, db.node2, candidateSet, toBeUpdated);


            NewFrontierEdge msg = new NewFrontierEdge(db.node1, db.node2, db.coreness1, db.coreness2, toBeUpdated);
            getOwner(db.node1).tell(msg, getSelf());
            getOwner(db.node2).tell(msg, getSelf());

            frontierEdges.markCompleted(db);
            frontierEdges.incrementLocalCoreness(toBeUpdated);


            tryNextFrontierEdge();
        }

        //}
    }

    /**
     * Handles the end of the algorithm. Terminates all worker and exits.
     */
    private void handleEndOfAlgorithm() {
        log.info("finished");
        corenessReceived = 0;
        for (ActorRef w : partitionToActor.values()) {
            w.tell(new FinalCorenessQuery(), getSelf());
        }
    }

    /**
     * Write result into output file
     *
     * @param corenessTable coreness table from partition
     * @throws IOException
     */
    private void writeResult(HashMap<Integer, Integer> corenessTable) throws IOException {
        Config conf = getContext().system().settings().config();
        FileWriter writer = new FileWriter(conf.getString("k-core.coreness-file"), true);
        for (Map.Entry<Integer, Integer> entry : corenessTable.entrySet()) {
            writer.write("Node ID:" + entry.getKey() + " coreness:" + entry.getValue() + "\n");
        }
        writer.close();
    }

    /**
     * Handle result of coreness query. Updates the database and try to progress.
     * @param message
     */
    private void handleCorenessReply(CorenessReply message) {

        frontierEdges.updateCorenessTable(message.map);

        tryNextFrontierEdge();

    }

    /**
     * Try to progess the algorithm. If some nodes are ready for candidate set construction, send query.
     * If all nodes have been processed, terminate the algorithm.
     */
    private void tryNextFrontierEdge() {
        if (!frontierEdges.processedEverything()) {
            for (FrontierEdge db : frontierEdges.readyForCandidateSet()) {
                this.askReachableNodes(db);
            }
        } else handleEndOfAlgorithm();

    }

    /**
     * Handle the creation of a partition actor. Store him into database, handle duplicates, stop request
     * repetition.
     * @param message
     */
    private void handleNewPartitionActor(NewPartitionActor message) {
        /*
        if (partitionToActor.containsKey(message.getId()){
            partitionToActor.get(message.getId()).tell(new Terminate, getSelf());
        }
        */
        partitionToActor.put(message.getId(), message.getActorRef());
        partitionMsgs.remove(message.getId());
    }

    /**
     * Handle the completion of the first part by the worker. Fire up second phase.
     * @param message
     */
    private void handleCorenessState(CorenessState message) {
        corenessReceived++;
        partitionToActor.put(message.getPartitionId(), getSender());
        log.info("received {} replies, partitionToActor size {}", corenessReceived, partitionToActor.size());
        if (corenessReceived == numPartitions) {
            log.info("starting phase 2");
            frontierEdges.initMergeTree(nodeToPartition);
            getAllFrontierEdgesCoreness();
        }
    }

    /**
     * Query coreness to all partition to initialize local frontier edge database
     */
    private void getAllFrontierEdgesCoreness() {
        ArrayList<Integer>[] frontierNodes;
        frontierNodes = new ArrayList[numPartitions];
        for (int i = 0; i < frontierNodes.length; i++) {
            frontierNodes[i] = new ArrayList<Integer>();
        }

        for (FrontierEdge fe : frontierEdges.getList()) {
            Integer part1 = nodeToPartition.get(fe.node1);
            frontierNodes[part1].add(fe.node1);
            Integer part2 = nodeToPartition.get(fe.node2);
            frontierNodes[part2].add(fe.node2);
        }
        for (int i = 0; i < frontierNodes.length; i++) {
            int querysize = 100;
            ActorRef worker2 = partitionToActor.get(i);
            for (int j = 0; j < frontierNodes[i].size(); j = j + querysize) {
                int min = (j + querysize < frontierNodes[i].size()) ? j + querysize : frontierNodes[i].size();
                ArrayList<Integer> slice = new ArrayList<Integer>(frontierNodes[i].subList(j, min));
                worker2.tell(new CorenessQuery(slice), getSelf());
            }
        }
    }

    /**
     * Query for the reachable nodes necessary to build the candidate set for a certain froniter edge
     * @param db frontier edge in question
     */
    private void askReachableNodes(FrontierEdge db) {
        if (db.coreness1 <= db.coreness2) {
            askReachableNodes(db.node1);
        }
        if (db.coreness2 <= db.coreness1) {
            askReachableNodes(db.node2);
        }
    }

    /**
     * Query for the reachable nodes for a certain node to the appropriate partition
     * @param node
     */
    private void askReachableNodes(int node) {
        ActorRef worker = getOwner(node);
        worker.tell(new ReachableNodesQuery(node), getSelf());
    }

    /**
     * Returns an ActorRef for the worker associated to the partition of a node
     * @param node
     * @return
     */
    private ActorRef getOwner(int node) {
        ActorRef worker = partitionToActor.get(nodeToPartition.get(node));
        return worker;
    }

}
