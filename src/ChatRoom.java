import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatRoom {
    private String name;
    private InetAddress address;
    private InetAddress owner;
    private List<InetAddress> members = new ArrayList<>();

    public String getName() {
        return name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public InetAddress getOwner() {
        return owner;
    }

    public List<InetAddress> getMembers() {
        return members;
    }

    public ChatRoom(String name, InetAddress address, InetAddress owner) {
        this.name = name;
        this.address = address;
        this.owner = owner;
        this.addMember(this.owner);
    }

    public void addMember(InetAddress address) {
        members.add(address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return name.equals(chatRoom.name);
    }
}
