import enums.ResponseCode;
import utils.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class MultCastMessageServer {
    ;
    static final SerializeConvert<ServerResponse> serverResponseSerializeConvert = new SerializeConvert<>();

    static List<ChatRoom> rooms = new ArrayList<>();
    static MulticastSocket mSocket = null;

    public static void main(String[] args) {

        String message;

        try {
            mSocket = new MulticastSocket(ConnectionConfigurations.MULTICAST_SERVER_PORT);

            System.out.println("Servidor: ouvindo porta UDP/" + ConnectionConfigurations.SERVER_PORT + ".");

            while (true) {
                DatagramPacket request = createDatagramPacket();
                mSocket.receive(request);

                message = new String(request.getData()).trim();
                ServerResponse serverResponse = deCodeMessage(message, request);

                System.out.println("Servidor: recebido '" + message + "'.");

                byte[] serializedResponse = serverResponseSerializeConvert.serialize(serverResponse);
                DatagramPacket reply = new DatagramPacket(serializedResponse, serializedResponse.length, request.getAddress(),
                        request.getPort());
                mSocket.send(reply);
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (mSocket != null)
                mSocket.close();
        }
    }

    private static DatagramPacket createDatagramPacket() {
        final int DEFAULT_BUFFER_SIZE = ConnectionConfigurations.DEFAULT_BUFFER_SIZE;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        return new DatagramPacket(buffer, buffer.length);
    }

    private static ServerResponse deCodeMessage(String message, DatagramPacket request) {
        String[] sliptedCommand = message.split(" ", 2);

        String command = sliptedCommand[0];

        String commandParams = null;
        if (sliptedCommand.length > 1)
            commandParams = sliptedCommand[1];

        switch (command) {
            case "/start":
                return new ServerResponse(ResponseCode.START_CONNECTION, "Conected");
            case "/newroom":
                if (commandParams != null) {
                    String[] params = commandParams.split(" ", 2);

                    if (params.length != 2) return new ServerResponse("Failed to joind room you need to send:\n" +
                            "/newroom <room-name>");

                    String roomName = params[0];
                    String ownerNick = params[1];

                    InetAddress address = createNewRoom(roomName, ownerNick, request.getAddress());
                    if (address != null) {
                        return new ServerResponse("Room " + roomName + " was created at addres " + address);
                    } else {
                        return new ServerResponse("Failed at creating room");
                    }
                }
            case "/join":
                if (commandParams != null) {
                    String[] params = commandParams.split(" ", 2);

                    if (params.length != 2) return new ServerResponse("Failed to joind room you need to send:\n" +
                            "/join <room-name>");

                    String roomName = params[0];
                    String memberNick = params[1];

                    ChatRoom chatRoom = join(roomName, memberNick, request.getAddress());
                    if (chatRoom != null) {
                        return new ServerResponse(ResponseCode.ROOM_ID, "Joined room", chatRoom);
                    } else {
                        return new ServerResponse("Room not found");
                    }
                }
            case "/allrooms":
                return new ServerResponse(ResponseCode.ALL_ROOMS, rooms);
            case "/end":
                return new ServerResponse(ResponseCode.END_CONNECTION, "Connection ended");
            default:
                return new ServerResponse("Invalid command");
        }


    }

    private static ChatRoom join(String roomName, String memberNick, InetAddress address) {
        for (ChatRoom room : rooms) {
            if (room.getName().equals(roomName)) {
                room.addUser(new User(address, memberNick));
                return room;
            }
        }
        return null;
    }

    private static InetAddress createNewRoom(String roomName, String ownerNick, InetAddress ownerAddress) {
        try {
            InetAddress address = InetAddress.getByName("224.1.1.1");
            ChatRoom newRoom = new ChatRoom(roomName, address, new User(ownerAddress, ownerNick));

            boolean find = false;

            for (ChatRoom room : rooms) {
                if (room.equals(newRoom)) {
                    find = true;
                    break;
                }
            }

            if (!find) {
                rooms.add(newRoom);
                mSocket.joinGroup(newRoom.getAddress());
                return address;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
