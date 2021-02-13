import enums.ResponseCode;
import utils.ConnectionConfigurations;
import utils.ServerResponse;
import utils.ServerResponseSerializeConvert;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class MultCastMessageServer {
    static final int SERVER_PORT = ConnectionConfigurations.SERVER_PORT;
    static final ServerResponseSerializeConvert serverResponseSerializeConvert = new ServerResponseSerializeConvert();

    static List<ChatRoom> rooms = new ArrayList<>();

    public static void main(String[] args) {

        String message;
        try (DatagramSocket aSocket = new DatagramSocket(SERVER_PORT)) {

            System.out.println("Servidor: ouvindo porta UDP/" + SERVER_PORT + ".");

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
        }
    }

    private static DatagramPacket createDatagramPacket() {
        final int DEFAULT_BUFFER_SIZE = ConnectionConfigurations.DEFAULT_BUFFER_SIZE;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        return new DatagramPacket(buffer, buffer.length);
    }

    private static ServerResponse deCodeMessage(String message, DatagramPacket request) {
        if (message.charAt(0) == '/') {
            String[] sliptedCommand = message.split("/", 2);
            sliptedCommand = sliptedCommand[1].split(" ", 2);

            String command = sliptedCommand[0];

            String newMessage = "";
            if (sliptedCommand.length > 1)
                newMessage = sliptedCommand[1];

            switch (command) {
                case "start":
                    return new ServerResponse(ResponseCode.START_CONNECTION, "Conected");
                case "newroom":
                    newMessage = newMessage.split(" ", 2)[0];
                    InetAddress address = createNewRoom(newMessage, request.getAddress());
                    if (address != null) {
                        return new ServerResponse("Room " + newMessage + " was created at addres " + address);
                    } else {
                        return new ServerResponse("Failed at creating room");
                    }
                case "end":
                    return new ServerResponse(ResponseCode.END_CONNECTION, "Connection ended");
                default:
                    return new ServerResponse(newMessage);
            }

        } else {
            return new ServerResponse(ResponseCode.KEEP_CONNECTION, message);
        }

    }

    private static InetAddress createNewRoom(String roomName, InetAddress owner) {
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
                rooms.add(newRoom);
                return address;
            } else {
                return null;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
}
