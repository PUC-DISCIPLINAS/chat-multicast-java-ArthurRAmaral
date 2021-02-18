package utils;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public class User implements Serializable {
    private InetAddress address;
    private int port;
    private String nickname;

    public User(InetAddress address, int port, String nickname) {
        this.address = address;
        this.port = port;
        this.nickname = nickname;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public String toString() {
        return nickname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(address, user.address) &&
                Objects.equals(port, user.port) &&
                Objects.equals(nickname, user.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, nickname);
    }
}
