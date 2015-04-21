package kcore.messages;

import akka.actor.ActorRef;

import java.io.Serializable;

/**
 * Created by chuzz on 4/16/15.
 */
public class FrontierEdge implements Serializable {
    public int node1;
    public int node2;
    ActorRef part1, part2;
}
