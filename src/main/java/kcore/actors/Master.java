package kcore.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;
import kcore.IntGraph;
import kcore.messages.CorenessState;
import kcore.messages.FilenameLoadPartition;
import kcore.messages.FrontierEdge;
import kcore.messages.NewPartitionActor;

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
    IntGraph graph;
    HashMap<Integer, Integer> nodeToPartition = new HashMap<Integer, Integer>();
    Map<Integer, ActorRef> partitionToActor = new HashMap<Integer, ActorRef>();
    Set<FrontierEdge> frontierEdges = new HashSet<FrontierEdge>();
    int numPartitions, corenessReceived;


    @Override
    public void preStart() {
        log.debug("Master starting");
        // start work
        int totalInstances = splitPartitionFiles("graphfile", "partfile");
        numPartitions = totalInstances;
        corenessReceived = 0;

        for (int i = 0; i < totalInstances; i++) {
            backend.tell(new FilenameLoadPartition("smallfile" + i), getSelf());
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
            try {
                partitionFiles[nodeToPartition.get(node1)].write("" + node1 + " " + node2 + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nodeToPartition.get(node2) != nodeToPartition.get(node1)) {
                try {
                    partitionFiles[nodeToPartition.get(node2)].write("" + node1 + " " + node2 + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }

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

        if (message instanceof CorenessState) {
            final CorenessState coreness = (CorenessState) message;
            if (++corenessReceived == numPartitions) {
                for (FrontierEdge fe : frontierEdges) {
                    Integer part1 = nodeToPartition.get(fe.node1);
                    ActorRef worker1 = partitionToActor.get(part1);
                    Integer part2 = nodeToPartition.get(fe.node2);
                    ActorRef worker2 = partitionToActor.get(part2);
                    
                    worker1.tell(fe, getSelf());
                    worker2.tell(fe, getSelf());
                }
            }
            log.info("received {} replies", corenessReceived);

        } else if (message instanceof NewPartitionActor) {
            final NewPartitionActor pa = (NewPartitionActor) message;
            partitionToActor.put(pa.getId(), pa.getActorRef());
        }
    }
}
