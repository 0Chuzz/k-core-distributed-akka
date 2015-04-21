package kcore.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import kcore.DistKCore;
import kcore.IntGraph;
import kcore.KShellFS;
import kcore.messages.CorenessState;
import kcore.messages.LoadPartition;

import java.io.File;

/**
 * Created by Stefano on 09/03/2015.
 */
public class Worker extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    boolean test = false;

    @Override
    public void preStart() {
        log.debug("Worker starting");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.debug(message.toString());
        if (message instanceof LoadPartition) {
            if (test) log.warning("multiple load partitions!");
            final LoadPartition msg = (LoadPartition) message;
            final String graphFile = "graphFile" + Integer.toString(msg.getPartitionId());
            final IntGraph graph = msg.getPartition();
            //transform the graph object IntGraph to a binary file
            DistKCore.graphFileConstruction(graph, graphFile);


            //step 1: execution of the standard k-core decomposition algorithm in parallel
            //System.out.println("****** Step 1: Computing the coreness in local partitions ******");
            KShellFS ks = new KShellFS();
            int[] corenessTable;
            try {
                corenessTable = ks.execute(graphFile);
            } finally {
                new File(graphFile).delete();
            }
            log.debug(corenessTable.toString());
            test = true;
            getSender().tell(new CorenessState(corenessTable, msg.getPartitionId()), getSelf());

        }
    }
}
