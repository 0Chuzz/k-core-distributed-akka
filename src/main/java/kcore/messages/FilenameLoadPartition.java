package kcore.messages;

import kcore.structures.Graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Stefano on 16/03/2015.
 */
public class FilenameLoadPartition extends LoadPartition {
    static int partitionIds = 0;
    private String filename;
    private int partitionId;

    public FilenameLoadPartition(String filename) {
        this.filename = filename;
        this.partitionId = partitionIds++;
    }

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
