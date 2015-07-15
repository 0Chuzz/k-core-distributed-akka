package kcore.messages;

import kcore.structures.GraphWithCandidateSet;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Compressed message containing the whole reachable subgraph
 */
public class WholeReachableNodesReply extends ReachableNodesReply implements Serializable {
    public WholeReachableNodesReply(GraphWithCandidateSet reachableSubgraph, int partitionId, int node, int coreness) {
        super(reachableSubgraph, partitionId, node, coreness);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        // default serialization
        oos.defaultWriteObject();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(new GZIPOutputStream(byteArrayOutputStream));
        stream.writeInt(this.coreness);
        stream.writeObject(this.graph);
        stream.writeInt(this.node);
        stream.writeInt(this.partition);
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
        coreness = stream.readInt();
        graph = (GraphWithCandidateSet) stream.readObject();
        node = stream.readInt();
        partition = stream.readInt();

    }
}
