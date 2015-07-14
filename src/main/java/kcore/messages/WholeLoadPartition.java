package kcore.messages;

import kcore.structures.Graph;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by chuzz on 7/14/15.
 */
public class WholeLoadPartition extends LoadPartition {
    Graph graph;
    int id;

    public WholeLoadPartition(String s, int i) {
        FilenameLoadPartition filenameLoadPartition = new FilenameLoadPartition(s, i);
        graph = filenameLoadPartition.getPartition();
        id = filenameLoadPartition.getPartitionId();

    }

    @Override
    public Graph getPartition() {
        return graph;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        // default serialization
        oos.defaultWriteObject();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(new GZIPOutputStream(byteArrayOutputStream));
        stream.writeInt(id);
        stream.writeObject(graph);
        stream.close();


        // compress _value to a byte[] using new ObjectOutputStream(new GZIPOutputStream(new ByteArrayOutputStream()))
        byte[] compValue = byteArrayOutputStream.toByteArray();

        oos.writeInt(compValue.length);
        oos.write(compValue);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        // default deserialization
        ois.defaultReadObject();

        byte[] compValue = new byte[ois.readInt()];
        ois.readFully(compValue);

        // decompress _value from byte[] using new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream()))
        ObjectInputStream stream = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(compValue)));
        id = stream.readInt();
        graph = (Graph) stream.readObject();


    }

    @Override
    public int getPartitionId() {
        return id;
    }
}
