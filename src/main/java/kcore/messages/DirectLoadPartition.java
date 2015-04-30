package kcore.messages;

import kcore.DistKCore;
import kcore.IntGraph;
import kcore.structures.Graph;

import java.io.IOException;

/**
 * Created by Stefano on 09/03/2015.
 */
public class DirectLoadPartition extends LoadPartition {
    IntGraph graph;

    public DirectLoadPartition(String filename) {

        try {
            this.graph = DistKCore.graphConstruction(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Graph getPartition() {
        return graph;
    }

    @Override
    public int getPartitionId() {
        return 0;
    }
}
