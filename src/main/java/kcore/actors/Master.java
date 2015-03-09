package kcore.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;
import kcore.DistKCore;
import kcore.IntGraph;
import kcore.messages.CorenessState;
import kcore.messages.DirectLoadPartition;

import java.io.IOException;

/**
 * Created by Stefano on 09/03/2015.
 */
public class Master extends UntypedActor {
    ActorRef backend = getContext().actorOf(FromConfig.getInstance().props(),
            "workerRouter");
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    IntGraph graph;
    @Override
    public void preStart(){
        try {
            graph = DistKCore.graphConstruction("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // start work
        for(int i = 0; i < 10; i++) {
            backend.tell(new DirectLoadPartition(graph),getSelf());
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        // collect results
        final CorenessState coreness = (CorenessState) message;
        log.debug(coreness.toString());
    }
}
