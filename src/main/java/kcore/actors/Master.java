package kcore.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.routing.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ConsistentHashingPool;
import akka.routing.FromConfig;
import kcore.DistKCore;
import kcore.IntGraph;
import kcore.messages.CorenessState;
import kcore.messages.DirectLoadPartition;
import kcore.messages.FilenameLoadPartition;

import java.io.IOException;

/**
 * Created by Stefano on 09/03/2015.
 */
public class Master extends UntypedActor {
    ActorRef backend; /*= getContext().actorOf(FromConfig.getInstance().props(),
            "workersRouter");*/
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    IntGraph graph;
    @Override
    public void preStart(){
        log.debug("Master starting");
        // start work
        int totalInstances = 10;
        int maxInstancesPerNode = 10;
        boolean allowLocalRoutees = true;
        String useRole = "backend";
        backend = getContext().actorOf(
                new ClusterRouterPool(new AdaptiveLoadBalancingPool(
                        SystemLoadAverageMetricsSelector.getInstance(), 0),
                        new ClusterRouterPoolSettings(totalInstances, maxInstancesPerNode,
                                allowLocalRoutees, useRole)).props(Props
                        .create(Worker.class)), "workersRouter");
        for(int i = 0; i < totalInstances; i++) {
            backend.tell(new FilenameLoadPartition("graphfile"),getSelf());
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        // collect results
        log.debug(message.toString());
        final CorenessState coreness = (CorenessState) message;

        if (coreness != null) log.info("Result: {}", coreness.toString());
    }
}
