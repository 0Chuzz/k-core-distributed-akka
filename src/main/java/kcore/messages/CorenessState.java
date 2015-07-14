package kcore.messages;

import java.io.Serializable;

/**
 * Partial coreness end of calculation signal
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
