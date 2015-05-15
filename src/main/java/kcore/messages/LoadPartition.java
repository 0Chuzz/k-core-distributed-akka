package kcore.messages;

import kcore.structures.Graph;

import java.io.Serializable;

/**
 * Created by Stefano on 09/03/2015.
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
