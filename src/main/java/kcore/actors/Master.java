package kcore.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.sysmsg.Terminate;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;
import kcore.messages.*;
import kcore.structures.FrontierEdgeDb;
import kcore.structures.GraphWithCandidateSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Stefano on 09/03/2015.
 */

public class Master extends UntypedActor {
    ActorRef backend = getContext().actorOf(FromConfig.getInstance().props(),
            "workersRouter");
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    HashMap<Integer, Integer> nodeToPartition = new HashMap<Integer, Integer>();
    Map<Integer, ActorRef> partitionToActor = new HashMap<Integer, ActorRef>();
    List<FrontierEdgeDb> frontierEdges = new ArrayList<FrontierEdgeDb>();
    int numPartitions, corenessReceived;


    @Override
    public void preStart() {
        log.debug("Master starting");
        // start work
        int totalInstances = splitPartitionFiles("graphfile", "singlefile");
        numPartitions = totalInstances;
        corenessReceived = 0;

        for (int i = 0; i < totalInstances; i++) {
            backend.tell(new FilenameLoadPartition("smallfile" + i, i), getSelf());
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
                FrontierEdgeDb e = new FrontierEdgeDb();
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

        if (message instanceof CorenessState) {
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
        FrontierEdgeDb db = frontierEdges.get(0);
        boolean updated = false;
        if (message.node == db.node1 && db.coreness1 <= db.coreness2) {
            db.candidateSet1 = message.graph;
            updated = true;
        } else if (message.node == db.node2 && db.coreness2 <= db.coreness1) {
            db.candidateSet2 = message.graph;
            updated = true;
        }


        if (updated) {
            GraphWithCandidateSet unionSet = new GraphWithCandidateSet();
            if (db.coreness1 <= db.coreness2) {
                if (db.candidateSet1 == null) return;
                unionSet.union(db.candidateSet1);
            }
            if (db.coreness2 <= db.coreness1) {
                if (db.candidateSet2 == null) return;
                unionSet.union(db.candidateSet2);
            }

            unionSet.pruneCandidateNodes();
            log.info("nodes to be updated: {}", unionSet.getCandidateSet());
            NewFrontierEdge msg = new NewFrontierEdge(db.node1, db.node2, db.coreness1, db.coreness2, unionSet.getCandidateSet());
            NewFrontierEdge msg2 = new NewFrontierEdge(db.node1, db.node2, db.coreness1, db.coreness2, unionSet.getCandidateSet());

            db.worker1.tell(msg, getSelf());
            db.worker2.tell(msg2, getSelf());
            frontierEdges.remove(0);
            for (FrontierEdgeDb db2 : frontierEdges) {
                if (unionSet.getCandidateSet().contains(db2.node1)) {
                    db2.coreness1++;
                }
                if (unionSet.getCandidateSet().contains(db2.node2)) {
                    db2.coreness2++;
                }
            }

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
        for (FrontierEdgeDb db : frontierEdges) {
        if (message.map.containsKey(db.node1)) {
            db.coreness1 = message.map.get(db.node1);
            db.worker1 = getSender();
        }
        if (message.map.containsKey(db.node2)) {
            db.coreness2 = message.map.get(db.node2);
            db.worker2 = getSender();
        }
        }
        tryNextFrontierEdge();

    }

    private void tryNextFrontierEdge() {
        if (frontierEdges.size() > 0) {
            FrontierEdgeDb db = frontierEdges.get(0);

            if (db.coreness1 != -1 && db.coreness2 != -1) {
                this.getReachableNodes(db);
            }
        } else handleEndOfAlgorithm();

    }

    private void handleNewPartitionActor(NewPartitionActor message) {
        partitionToActor.put(message.getId(), message.getActorRef());
    }

    private void handleCorenessState(CorenessState message) {
        corenessReceived++;
        if (corenessReceived == numPartitions) {
            getAllFrontierEdgesCoreness();
        }
        log.info("received {} replies", corenessReceived);
    }

    private void getAllFrontierEdgesCoreness() {
        ArrayList<Integer>[] frontierNodes;
        frontierNodes = new ArrayList[numPartitions];
        for (int i = 0; i < frontierNodes.length; i++) {
            frontierNodes[i] = new ArrayList<Integer>();
        }

        for (FrontierEdgeDb fe : frontierEdges) {
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

    private void getReachableNodes(FrontierEdgeDb db) {
        if (db.coreness1 <= db.coreness2) {
            db.worker1.tell(new ReachableNodesQuery(db.node1, db.coreness1), getSelf());
        }
        if (db.coreness2 <= db.coreness1) {
            db.worker2.tell(new ReachableNodesQuery(db.node2, db.coreness2), getSelf());
        }
    }


}
