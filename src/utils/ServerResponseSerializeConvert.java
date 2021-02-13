package utils;

import interfaces.SerializeCoverter;

import java.io.*;

public class ServerResponseSerializeConvert implements SerializeCoverter<ServerResponse> {
    public byte[] serialize(ServerResponse obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public ServerResponse deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (ServerResponse) is.readObject();
    }
}
