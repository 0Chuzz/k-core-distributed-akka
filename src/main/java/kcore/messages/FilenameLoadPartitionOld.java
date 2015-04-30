package kcore.messages;

import kcore.DistKCore;
import kcore.structures.Graph;

import java.io.IOException;

/**
 * Created by Stefano on 16/03/2015.
 */
public class FilenameLoadPartitionOld extends LoadPartition {
    static int partitionIds = 0;
    private String filename;
    private int partitionId;

    public FilenameLoadPartitionOld(String filename) {
        this.filename = filename;
        this.partitionId = partitionIds++;
    }

    @Override
    public Graph getPartition() {
        try {
            return DistKCore.graphConstruction(filename);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getPartitionId() {
        return partitionId;
    }
}
