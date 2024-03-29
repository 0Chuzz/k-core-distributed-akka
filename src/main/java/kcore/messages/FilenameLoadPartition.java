package kcore.messages;

import kcore.structures.Graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Load a partition from a local file
 */
public class FilenameLoadPartition extends LoadPartition {
    /**
     * filename that will be loaded
     */
    private String filename;
    /**
     * id of the partition
     */
    private int partitionId;

    public FilenameLoadPartition(String filename, int partitionId) {
        this.filename = filename;
        this.partitionId = partitionId;
    }

    /**
     * Read edges from  a text file
     *
     * @return
     */
    @Override
    public Graph getPartition() {
        try {
            //int startFrom;
            String line;

            BufferedReader reader = new BufferedReader(new FileReader(filename));

            Graph gr = new Graph();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s");
                gr.addEdge(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }

            reader.close();
            return gr;

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
