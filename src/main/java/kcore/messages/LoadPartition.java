package kcore.messages;

import kcore.structures.Graph;

import java.io.Serializable;

/**
 * Abstract partition loader
 */
abstract public class LoadPartition implements Serializable {
    /**
     * load the partition, somehow
     *
     * @return
     */
    public abstract Graph getPartition();

    /**
     * get unique partition id
     * @return
     */
    public abstract int getPartitionId();
}
