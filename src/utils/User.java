package utils;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public class User implements Serializable {
    InetAddress address;
    String nickname;

    public User(InetAddress address, String nickname) {
        this.address = address;
        this.nickname = nickname;
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
                Objects.equals(nickname, user.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, nickname);
    }
}
