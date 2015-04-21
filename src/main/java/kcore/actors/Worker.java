package kcore.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import kcore.DistKCore;
import kcore.IntGraph;
import kcore.KShellFS;
import kcore.messages.CorenessState;
import kcore.messages.FrontierEdge;
import kcore.messages.LoadPartition;

import java.io.File;

/**
 * Created by Stefano on 09/03/2015.
 */
public class Worker extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    int[] corenessTable;
    int partitionId;

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
            final String graphFile = "graphFile" + Integer.toString(msg.getPartitionId());
            final IntGraph graph = msg.getPartition();
            //transform the graph object IntGraph to a binary file
            DistKCore.graphFileConstruction(graph, graphFile);


            //step 1: execution of the standard k-core decomposition algorithm in parallel
            //System.out.println("****** Step 1: Computing the coreness in local partitions ******");
            KShellFS ks = new KShellFS();

            try {
                corenessTable = ks.execute(graphFile);
            } finally {
                new File(graphFile).delete();
            }
            log.info("partial coreness" + corenessToString());
            CorenessState corenessState;
            corenessState = new CorenessState(msg.getPartitionId());
            getSender().tell(corenessState, getSelf());

        } else if (message instanceof FrontierEdge) {
            final FrontierEdge fe = (FrontierEdge) message;
            log.info("I am {}  and have a frontier edge from {} to {}", partitionId, fe.node1, fe.node2);
        }
    }


    public String corenessToString() {
        StringBuilder b = new StringBuilder();
        b.append("[");
        for (int i : corenessTable) {
            b.append(i);
            b.append(", ");
        }
        b.append("]");
        return b.toString();
    }
}
