package utils;

import interfaces.SerializeCoverter;

import java.io.*;

public class SerializeConvert<T extends Serializable>{
    public byte[] serialize(T obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public T deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        try {
            return (T) is.readObject();
        } catch (Exception e) {
            return null;
        }
    }
}
