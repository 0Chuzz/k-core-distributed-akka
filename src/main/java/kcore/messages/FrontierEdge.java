package kcore.messages;

import akka.actor.ActorRef;

import java.io.Serializable;

/**
 * Created by chuzz on 4/16/15.
 */
public class FrontierEdge implements Serializable {
    int node1, node2;
    ActorRef part1, part2;
}
