package kcore.messages;

import java.io.Serializable;

/**
 * Created by Stefano on 09/03/2015.
 */
public class CorenessState implements Serializable {
    private final int[] coreness;
    private int partitionId;

    public CorenessState(int[] corenessTable, int partitionId) {
        coreness = corenessTable;
        this.partitionId = partitionId;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("[");
        for (int i : coreness) {
            b.append(i);
            b.append(", ");
        }
        b.append("]");
        return b.toString();
    }

    public int getPartitionId() {
        return partitionId;
    }
}
