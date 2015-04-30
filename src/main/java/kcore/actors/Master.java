package kcore.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;
import kcore.messages.*;
import kcore.structures.GraphWithCandidateSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Stefano on 09/03/2015.
 */
class FrontierEdgeDb {
    int node1, node2;
    int coreness1 = -1, coreness2 = -1;
    ActorRef worker1, worker2;
    GraphWithCandidateSet candidateSet1, candidateSet2;
}
public class Master extends UntypedActor {
    ActorRef backend = getContext().actorOf(FromConfig.getInstance().props(),
            "workersRouter");
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    HashMap<Integer, Integer> nodeToPartition = new HashMap<Integer, Integer>();
    Map<Integer, ActorRef> partitionToActor = new HashMap<Integer, ActorRef>();
    Set<FrontierEdgeDb> frontierEdges = new HashSet<FrontierEdgeDb>();
    int numPartitions, corenessReceived;


    @Override
    public void preStart() {
        log.debug("Master starting");
        // start work
        int totalInstances = splitPartitionFiles("graphfile", "partfile");
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
            final CorenessState coreness = (CorenessState) message;
            if (++corenessReceived == numPartitions) {
                getAllFrontierEdgesCoreness();
            }
            log.debug("received {} replies", corenessReceived);

        } else if (message instanceof NewPartitionActor) {
            final NewPartitionActor pa = (NewPartitionActor) message;
            partitionToActor.put(pa.getId(), pa.getActorRef());
        } else if (message instanceof CorenessReply) {
            final CorenessReply r = (CorenessReply) message;
            updateCorenessTable(r);
        } else if (message instanceof ReachableNodesReply) {
            final ReachableNodesReply reply = (ReachableNodesReply) message;
            updateCorenessTable(reply);
            //log.info("reachable from {} w coreness {} :{}", reply.node, reply.coreness, reply.graph.getCandidateSet());
        }
    }

    private void updateCorenessTable(ReachableNodesReply reply) {
        for (FrontierEdgeDb db : frontierEdges) {
            boolean updated = false;
            if (reply.node == db.node1 && db.coreness1 <= db.coreness2) {
                db.candidateSet1 = reply.graph;
                updated = true;
            } else if (reply.node == db.node2 && db.coreness2 <= db.coreness1) {
                db.candidateSet2 = reply.graph;
                updated = true;
            }


            if (updated) {
                GraphWithCandidateSet unionSet = new GraphWithCandidateSet();
                if (db.coreness1 <= db.coreness2) {
                    if (db.candidateSet1 == null) continue;
                    unionSet.union(db.candidateSet1);
                }
                if (db.coreness2 <= db.coreness1) {
                    if (db.candidateSet2 == null) continue;
                    unionSet.union(db.candidateSet2);
                }
                unionSet.pruneCandidateNodes();
                // TODO: update coreness
                log.info("nodes to be updated: {}", unionSet.getCandidateSet());

            }

        }
    }

    private void updateCorenessTable(CorenessReply r) {
        for (FrontierEdgeDb db : frontierEdges) {
            if (r.map.containsKey(db.node1)) {
                db.coreness1 = r.map.get(db.node1);
                db.worker1 = getSender();
            }
            if (r.map.containsKey(db.node2)) {
                db.coreness2 = r.map.get(db.node2);
                db.worker2 = getSender();
            }

            if (db.coreness1 != -1 && db.coreness2 != -1) {
                this.getReachableNodes(db);
            }
        }
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
