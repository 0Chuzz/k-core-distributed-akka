package kcore.messages;

import kcore.IntGraph;

/**
 * Created by Stefano on 09/03/2015.
 */
public class DirectLoadPartition extends LoadPartition {
    final IntGraph graph;

    public DirectLoadPartition(IntGraph graph) {
        this.graph = graph;
    }

    @Override
    public IntGraph getPartition() {
        return graph;
    }
}
