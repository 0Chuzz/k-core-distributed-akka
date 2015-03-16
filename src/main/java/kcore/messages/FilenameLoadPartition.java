package kcore.messages;

import kcore.DistKCore;
import kcore.IntGraph;

import java.io.IOException;

/**
 * Created by Stefano on 16/03/2015.
 */
public class FilenameLoadPartition extends LoadPartition {
    private String filename;

    public FilenameLoadPartition(String filename) {
        this.filename = filename;
    }

    @Override
    public IntGraph getPartition() {
        try {
            return DistKCore.graphConstruction(filename);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
