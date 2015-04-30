package kcore.messages;

import kcore.structures.Graph;

import java.io.Serializable;

/**
 * Created by Stefano on 09/03/2015.
 */
abstract public class LoadPartition implements Serializable {
    public abstract Graph getPartition();

    public abstract int getPartitionId();
}
