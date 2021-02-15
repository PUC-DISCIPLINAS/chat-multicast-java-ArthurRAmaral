package utils;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class ChatRoom implements Serializable {
    static final SerializeConvert<ChatMessage> chatMessageSerializeConvert = new SerializeConvert<>();
    final private String name;
    final private User owner;

    private InetAddress address;
    private List<User> users = new ArrayList<>();

    public ChatRoom(String name, InetAddress address, User owner) {
        this.name = name;
        this.address = address;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public User getOwner() {
        return owner;
    }

    public List<User> getMembers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return name.equals(chatRoom.name);
    }

    @Override
    public String toString() {
        return "\n{" +
                "Name= " + name +
                ", Owner= " + owner +
                "}\n";
    }

    public void listenRoom(MulticastSocket mSocket) {
        new Thread(() -> {
            while (true) {
                byte[] buffer = new byte[ConnectionConfigurations.DEFAULT_BUFFER_SIZE];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                try {
                    mSocket.receive(messageIn);
                    ChatMessage message = chatMessageSerializeConvert.deserialize(messageIn.getData());
                    System.out.println(message.getOwner().getNickname() + ": " + message.getText());
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean removeUserByAdress(InetAddress userAddress) {
        return this.getMembers().removeIf((user) -> user.address.equals(userAddress));
    }
}
