package kcore.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.routing.AdaptiveLoadBalancingPool;
import akka.cluster.routing.ClusterRouterPool;
import akka.cluster.routing.ClusterRouterPoolSettings;
import akka.cluster.routing.SystemLoadAverageMetricsSelector;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import kcore.IntGraph;
import kcore.messages.CorenessState;
import kcore.messages.FilenameLoadPartition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Stefano on 09/03/2015.
 */
public class Master extends UntypedActor {
    ActorRef backend; /*= getContext().actorOf(FromConfig.getInstance().props(),
            "workersRouter");*/
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    IntGraph graph;

    @Override
    public void preStart() {
        log.debug("Master starting");
        // start work
        int totalInstances = splitPartitionFiles("graphfile", "partfile");
        int maxInstancesPerNode = totalInstances;
        boolean allowLocalRoutees = true;
        String useRole = "backend";
        backend = getContext().actorOf(
                new ClusterRouterPool(new AdaptiveLoadBalancingPool(
                        SystemLoadAverageMetricsSelector.getInstance(), 0),
                        new ClusterRouterPoolSettings(totalInstances, maxInstancesPerNode,
                                allowLocalRoutees, useRole)).props(Props
                        .create(Worker.class)), "workersRouter");
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
        HashMap<Integer, Integer> nodeToPartition = new HashMap<Integer, Integer>();
        Set<Integer> partitions = new HashSet<Integer>();
        FileWriter out;
        while (reader.hasNextInt()) {
            int node = reader.nextInt();
            int partition = reader.nextInt();
            nodeToPartition.put(node, partition);
            partitions.add(partition);
        }


        FileWriter[] partitionFiles = new FileWriter[partitions.size()];
        for (int p : partitions) {
            try {
                partitionFiles[p - 1] = new FileWriter("smallfile" + (p - 1));
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
                partitionFiles[nodeToPartition.get(node1) - 1].write("" + node1 + " " + node2 + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nodeToPartition.get(node2) != nodeToPartition.get(node1)) try {
                partitionFiles[nodeToPartition.get(node2) - 1].write("" + node1 + " " + node2 + "\n");
            } catch (IOException e) {
                e.printStackTrace();
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
        final CorenessState coreness = (CorenessState) message;

        if (coreness != null) log.info("Result: {}", coreness.toString());
    }
}
