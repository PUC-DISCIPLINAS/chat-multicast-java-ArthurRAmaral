package interfaces;

import java.io.IOException;
import java.io.Serializable;

public interface SerializeCoverter<T extends Serializable> {
    byte[] serialize(T obj) throws IOException;

    T deserialize(byte[] data) throws IOException, ClassNotFoundException;
}
