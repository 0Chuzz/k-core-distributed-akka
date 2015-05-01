package kcore.structures;

import akka.actor.ActorRef;

/**
 * Created by Stefano on 09/03/2015.
 */
public class FrontierEdgeDb {
    public int node1, node2;
    public int coreness1 = -1, coreness2 = -1;
    public ActorRef worker1, worker2;
    public GraphWithCandidateSet candidateSet1, candidateSet2;
}
