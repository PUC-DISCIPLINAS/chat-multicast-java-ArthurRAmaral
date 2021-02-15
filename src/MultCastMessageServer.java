import enums.ResponseCode;
import utils.*;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class MultCastMessageServer {
    ;
    static final SerializeConvert<ServerResponse> serverResponseSerializeConvert = new SerializeConvert<>();

    static List<ChatRoom> rooms = new ArrayList<>();
    static MulticastSocket mSocket = null;
    static DatagramSocket aSocket = null;

    public static void main(String[] args) {

        String message;

        try {
            aSocket = new DatagramSocket(ConnectionConfigurations.SERVER_PORT);

            System.out.println("Servidor: ouvindo porta UDP/" + ConnectionConfigurations.SERVER_PORT + ".");

            while (true) {
                DatagramPacket request = createDatagramPacket();
                aSocket.receive(request);

                message = new String(request.getData()).trim();
                ServerResponse serverResponse = deCodeMessage(message, request);

                System.out.println("Servidor: recebido '" + message + "'.");

                byte[] serializedResponse = serverResponseSerializeConvert.serialize(serverResponse);
                DatagramPacket reply = new DatagramPacket(serializedResponse, serializedResponse.length, request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
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

                    ChatRoom chatRoom = createNewRoom(roomName, new User(request.getAddress(), ownerNick));

                    if (chatRoom != null) {
                        return new ServerResponse(ResponseCode.START_CHAT_ROOM, chatRoom);
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

                    ChatRoom chatRoom = join(roomName, new User(request.getAddress(), memberNick));
                    if (chatRoom != null) {
                        return new ServerResponse(ResponseCode.START_CHAT_ROOM, chatRoom);
                    } else {
                        return new ServerResponse("Room not found");
                    }
                }
            case "/leave":
                if (commandParams != null) {
                    String[] params = commandParams.split(" ", 2);

                    if (params.length != 1) return new ServerResponse("Error: params are wrong");

                    String roomName = params[0];

                    boolean work = leave(roomName, request.getAddress());

                    if (work) {
                        return new ServerResponse(ResponseCode.END_CHAT_ROOM);
                    } else {
                        return new ServerResponse("Room not found or user not registered in this room");
                    }
                } else {
                    return new ServerResponse("Error: Missed room name");
                }
            case "/allrooms":
                return new ServerResponse(ResponseCode.ALL_ROOMS, rooms);
            case "/end":
                return new ServerResponse(ResponseCode.END_CONNECTION, "Connection ended");
            default:
                return new ServerResponse("Invalid command");
        }


    }

    private static boolean leave(String roomName, InetAddress memberAddress) {
        for (ChatRoom room : rooms) {
            if (room.getName().equals(roomName)) {
                return room.removeUserByAdress(memberAddress);
            }
        }
        return false;
    }

    private static ChatRoom join(String roomName, User member) {
        for (ChatRoom room : rooms) {
            if (room.getName().equals(roomName)) {
                room.addUser(member);
                return room;
            }
        }
        return null;
    }

    private static ChatRoom createNewRoom(String roomName, User owner) {
        try {
            InetAddress address = InetAddress.getByName("224.1.1.1");
            ChatRoom newRoom = new ChatRoom(roomName, address, owner);

            boolean find = false;

            for (ChatRoom room : rooms) {
                if (room.equals(newRoom)) {
                    find = true;
                    break;
                }
            }

            if (!find) {
                newRoom.addUser(owner);
                rooms.add(newRoom);
                return newRoom;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
