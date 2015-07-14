package kcore.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
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
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof LoadPartition) {
            LoadPartition lp = (LoadPartition) message;
            ActorRef actorRef = context().actorOf(Props.create(Worker.class));
            log.info("Creating actor {} for partition {}", actorRef, lp.getPartitionId());
            actorRef.tell(message, getSender());
            getSender().tell(new NewPartitionActor(lp.getPartitionId(), actorRef), getSelf());
        }
    }
}
