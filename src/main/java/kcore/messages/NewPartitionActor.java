package kcore.messages;

import akka.actor.ActorRef;

import java.io.Serializable;

/**
 * Created by chuzz on 4/21/15.
 */
public class NewPartitionActor implements Serializable {

    private final ActorRef actorRef;
    private final int id;

    public NewPartitionActor(int id, ActorRef actorRef) {
        this.id = id;
        this.actorRef = actorRef;
    }

    public ActorRef getActorRef() {
        return actorRef;
    }

    public int getId() {
        return id;
    }
}
