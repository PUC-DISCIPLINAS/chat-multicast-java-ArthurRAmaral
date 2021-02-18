import enums.ResponseCode;
import utils.*;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class MulticastMessageServer {
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
                DatagramPacket response = new DatagramPacket(serializedResponse, serializedResponse.length, request.getAddress(),
                        request.getPort());
                aSocket.send(response);
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

        String paramsErrorMessage = "";

        switch (command) {
            case Commands.start:
                paramsErrorMessage = "Failed to connect.";
                if (commandParams != null) {
                    String[] params = commandParams.split(" ", 1);

                    if (params.length != 1) return new ServerResponse(paramsErrorMessage);
                    String nickname = params[0];

                    return new ServerResponse(ResponseCode.START_CONNECTION, "Conected", new User(request.getAddress(), request.getPort(), nickname));
                } else {
                    return new ServerResponse(paramsErrorMessage);
                }
            case Commands.newRoom:
                paramsErrorMessage = "Failed to create room. You need to send:\n" +
                        "/newroom <room-name> <group-address>";
                if (commandParams != null) {
                    String[] params = commandParams.split(" ", 3);

                    if (params.length != 3) return new ServerResponse(paramsErrorMessage);

                    String roomName = params[0];
                    String groupAddress = params[1];
                    String ownerNick = params[2];

                    ChatRoom chatRoom = createNewRoom(roomName, new User(request.getAddress(), request.getPort(), ownerNick), groupAddress);

                    if (chatRoom != null) {
                        return new ServerResponse(ResponseCode.START_CHAT_ROOM, chatRoom);
                    } else {
                        return new ServerResponse("Already exits a room with this name or address");
                    }
                } else {
                    return new ServerResponse(paramsErrorMessage);
                }
            case Commands.join:
                paramsErrorMessage = "Failed to join room. You need to send:\n" +
                        "/join <room-name>";
                if (commandParams != null) {
                    String[] params = commandParams.split(" ", 2);

                    if (params.length != 2) return new ServerResponse(paramsErrorMessage);

                    String roomName = params[0];
                    String memberNick = params[1];

                    ChatRoom chatRoom = join(roomName, new User(request.getAddress(), request.getPort(), memberNick));
                    if (chatRoom != null) {
                        return new ServerResponse(ResponseCode.START_CHAT_ROOM, chatRoom);
                    } else {
                        return new ServerResponse("Room not found");
                    }
                } else {
                    return new ServerResponse(paramsErrorMessage);
                }
            case Commands.leave:
                paramsErrorMessage = "Failed to leave room.";

                if (commandParams != null) {
                    String[] params = commandParams.split(" ", 2);

                    if (params.length != 2) return new ServerResponse(paramsErrorMessage);

                    String roomName = params[0];
                    String nickName = params[1];

                    boolean work = leave(roomName, new User(request.getAddress(), request.getPort(), nickName));

                    if (work) {
                        return new ServerResponse(ResponseCode.END_CHAT_ROOM);
                    } else {
                        return new ServerResponse("Room not found or user not registered in this room");
                    }
                } else {
                    return new ServerResponse(paramsErrorMessage);
                }
            case Commands.allRooms:
                return new ServerResponse(ResponseCode.ALL_ROOMS, rooms);
            case Commands.end:
                return new ServerResponse(ResponseCode.END_CONNECTION, "Connection ended");
            case Commands.members:
                paramsErrorMessage = "Failed to get room members.";
                if (commandParams != null) {
                    String[] params = commandParams.split(" ", 2);

                    if (params.length != 1) return new ServerResponse(paramsErrorMessage);

                    String roomName = params[0];
                    for (ChatRoom room : rooms) {
                        if (room.getName().equals(roomName)) {
                            return new ServerResponse(ResponseCode.MEMBERS, room);
                        }
                    }
                    return new ServerResponse("Room not found");
                } else {
                    return new ServerResponse(paramsErrorMessage);
                }
            default:
                return new ServerResponse("Invalid command");
        }


    }

    private static boolean leave(String roomName, User user) {
        for (ChatRoom room : rooms) {
            if (room.getName().equals(roomName)) {
                return room.removeUser(user);
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

    private static ChatRoom createNewRoom(String roomName, User owner, String groupAddress) {
        try {
            InetAddress address = InetAddress.getByName(groupAddress);
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
