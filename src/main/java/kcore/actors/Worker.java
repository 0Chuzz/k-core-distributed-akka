package kcore.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import kcore.DistKCore;
import kcore.IntGraph;
import kcore.KShellFS;
import kcore.messages.CorenessState;
import kcore.messages.LoadPartition;

/**
 * Created by Stefano on 09/03/2015.
 */
public class Worker extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void preStart(){
        log.debug("Worker starting");
    }
    @Override
    public void onReceive(Object message) throws Exception {
        log.debug(message.toString());
        final LoadPartition msg = (LoadPartition) message;
        final String graphFile = "graphFile2";
        if (msg != null){
            final IntGraph graph = msg.getPartition();
            //transform the graph object IntGraph to a binary file
            DistKCore.graphFileConstruction(graph, graphFile);


            //step 1: execution of the standard k-core decomposition algorithm in parallel
            //System.out.println("****** Step 1: Computing the coreness in local partitions ******");
            KShellFS ks = new KShellFS();
            int[] corenessTable=ks.execute(graphFile);
            log.debug(corenessTable.toString());
            getSender().tell(new CorenessState(corenessTable), getSelf());
        }
    }
}
