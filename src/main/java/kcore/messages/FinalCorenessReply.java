package kcore.messages;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Compressed message containing the final partition table.
 */
public class FinalCorenessReply implements Serializable {
    public HashMap<Integer, Integer> table;

    public FinalCorenessReply(HashMap<Integer, Integer> integerIntegerHashMap) {
        table = integerIntegerHashMap;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        // default serialization
        oos.defaultWriteObject();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(new GZIPOutputStream(byteArrayOutputStream));
        stream.writeObject(table);
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
        table = (HashMap<Integer, Integer>) stream.readObject();


    }

}
