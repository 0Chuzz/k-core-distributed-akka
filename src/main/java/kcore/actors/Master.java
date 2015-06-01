package kcore.actors;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.dispatch.sysmsg.Terminate;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;
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
 * Created by Stefano on 09/03/2015.
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
    HashMap<Integer, FilenameLoadPartition> partitionMsgs;
    int numPartitions, corenessReceived;

    @Override
    public void postStop() {
        tick.cancel();
    }

    @Override
    public void preStart() {
        log.debug("Master starting");
        // start work
        int totalInstances = splitPartitionFiles("graphfile", "partfile");
        numPartitions = totalInstances;
        corenessReceived = 0;
        partitionMsgs = new HashMap<Integer, FilenameLoadPartition>();
        for (int i = 0; i < totalInstances; i++) {
            partitionMsgs.put(i, new FilenameLoadPartition("smallfile" + i, i));
        }

    }

    private int splitPartitionFiles(String graphfile, String partfile) {
        Scanner reader;

        try {
            reader = new Scanner(new File(partfile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
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
            e.printStackTrace();
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
            for (FilenameLoadPartition msg : partitionMsgs.values()) {
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

        }
    }

    private void handleReachableNodesReply(ReachableNodesReply message) {
        //for (FrontierEdgeDb db : frontierEdges) {

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

    private void handleEndOfAlgorithm() {
        log.info("finished");
        for (ActorRef w : partitionToActor.values()) {
            w.tell(new Terminate(), getSelf());
        }
        //Cluster.get(getContext().system()).shutdown();
    }

    private void handleCorenessReply(CorenessReply message) {

        frontierEdges.updateCorenessTable(message.map);

        tryNextFrontierEdge();

    }

    private void tryNextFrontierEdge() {
        if (!frontierEdges.processedEverything()) {
            for (FrontierEdge db : frontierEdges.readyForCandidateSet()) {
                this.askReachableNodes(db);
            }
        } else handleEndOfAlgorithm();

    }

    private void handleNewPartitionActor(NewPartitionActor message) {
        partitionToActor.put(message.getId(), message.getActorRef());
        partitionMsgs.remove(message.getId());
    }

    private void handleCorenessState(CorenessState message) {
        corenessReceived++;
        partitionToActor.put(message.getPartitionId(), getSender());
        log.info("received {} replies, partitionToActor size {}", corenessReceived, partitionToActor.size());
        if (corenessReceived == numPartitions) {
            getAllFrontierEdgesCoreness();
        }
    }

    private void getAllFrontierEdgesCoreness() {
        ArrayList<Integer>[] frontierNodes;
        frontierNodes = new ArrayList[numPartitions];
        for (int i = 0; i < frontierNodes.length; i++) {
            frontierNodes[i] = new ArrayList<Integer>();
        }

        for (FrontierEdge fe : frontierEdges) {
            Integer part1 = nodeToPartition.get(fe.node1);
            frontierNodes[part1].add(fe.node1);
            Integer part2 = nodeToPartition.get(fe.node2);
            frontierNodes[part2].add(fe.node2);
        }
        for (int i = 0; i < frontierNodes.length; i++) {

            ActorRef worker2 = partitionToActor.get(i);

            worker2.tell(new CorenessQuery(frontierNodes[i]), getSelf());
        }
    }

    private void askReachableNodes(FrontierEdge db) {
        if (db.coreness1 <= db.coreness2) {
            askReachableNodes(db.node1);
        }
        if (db.coreness2 <= db.coreness1) {
            askReachableNodes(db.node2);
        }
    }

    private void askReachableNodes(int node) {
        ActorRef worker = getOwner(node);
        worker.tell(new ReachableNodesQuery(node), getSelf());
    }

    private ActorRef getOwner(int node) {
        ActorRef worker = partitionToActor.get(nodeToPartition.get(node));
        return worker;
    }

}
