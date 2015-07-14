package kcore.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import kcore.messages.LoadPartition;
import kcore.messages.NewPartitionActor;

/**
 * Worker actor creator
 */
public class WorkerCreator extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void preStart() {
        log.info("{} starting", this);
        Cluster.get(getContext().system()).subscribe(getSelf(), ClusterEvent.MemberExited.class);
    }

    @Override
    public void postStop() {
        Cluster.get(getContext().system()).shutdown();
        getContext().system().shutdown();
        System.exit(0);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof LoadPartition) {
            LoadPartition lp = (LoadPartition) message;
            ActorRef actorRef = context().actorOf(Props.create(Worker.class));
            log.info("Creating actor {} for partition {}", actorRef, lp.getPartitionId());
            actorRef.tell(message, getSender());
            getSender().tell(new NewPartitionActor(lp.getPartitionId(), actorRef), getSelf());
        } else if (message instanceof ClusterEvent.MemberExited) {
            log.info("worker creator exiting");
            Cluster.get(getContext().system()).shutdown();
            getContext().system().shutdown();
            System.exit(0);
        }
    }
}
