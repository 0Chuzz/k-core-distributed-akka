package kcore.messages;

import java.io.Serializable;

/**
 * Created by Stefano on 09/03/2015.
 */
public class CorenessState implements Serializable {
    /**
     * partition id of the signaling actor
     */
    private int partitionId;

    public CorenessState(int partitionId) {
        //coreness = corenessTable;
        this.partitionId = partitionId;
    }


    public int getPartitionId() {
        return partitionId;
    }
}
