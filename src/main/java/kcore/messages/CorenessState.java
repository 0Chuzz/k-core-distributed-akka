package kcore.messages;

import akka.event.LoggingAdapter;

import java.io.Serializable;

/**
 * Created by Stefano on 09/03/2015.
 */
public class CorenessState implements Serializable {
    private final int[] coreness;

    public CorenessState(int[] corenessTable) {
        coreness = corenessTable;
    }
}
