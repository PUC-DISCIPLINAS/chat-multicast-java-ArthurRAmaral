package interfaces;

import java.io.IOException;
import java.io.Serializable;

public interface SerializeConverter<T extends Serializable> {
    byte[] serialize(T obj) throws IOException;

    T deserialize(byte[] data) throws IOException, ClassNotFoundException;
}
