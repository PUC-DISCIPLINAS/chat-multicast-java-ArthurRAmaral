import enums.ResponseCode;
import utils.*;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Scanner;

public class MulticastMessageClient {
    static final SerializeConvert<ServerResponse> serverResponseSerializeConvert = new SerializeConvert<>();
    static final SerializeConvert<ChatMessage> chatMessageSerializeConvert = new SerializeConvert<>();
    static final SerializeConvert<User> userSerializeConvert = new SerializeConvert<>();
    static final Scanner read = new Scanner(System.in);
    static String myNickname = "";
    static InetAddress myAddress;

    static byte[] buffer = new byte[ConnectionConfigurations.DEFAULT_BUFFER_SIZE];

    static DatagramPacket request;
    static DatagramPacket response = new DatagramPacket(buffer, buffer.length);

    static ServerResponse serverResponse;
    static DatagramSocket datagramSocket;

    static ChatRoom actualRoom;
    static User me;

    public static void main(String[] args) {
        try {
            datagramSocket = new DatagramSocket();
            myAddress = InetAddress.getByName(ConnectionConfigurations.SERVER_ADDRESS);

            System.out.print("Insert your name: ");
            myNickname = read.next();
            String initialCommand = Commands.start + " " + myNickname;

            request = new DatagramPacket(initialCommand.getBytes(), initialCommand.length(), myAddress, ConnectionConfigurations.SERVER_PORT);

            sendDataToServer();

            if (serverResponse.getCode() != ResponseCode.START_CONNECTION) {
                return;
            }

            read.nextLine();

            do {
                String comand = read.nextLine();

                comand += " " + myNickname;

                request = new DatagramPacket(comand.getBytes(), comand.length(), myAddress, ConnectionConfigurations.SERVER_PORT);

                sendDataToServer();
            } while (serverResponse.getCode() != ResponseCode.END_CONNECTION);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (datagramSocket != null)
                datagramSocket.close();
        }
    }

    private static void sendDataToServer() {
        try {
            datagramSocket.send(request);
            datagramSocket.receive(response);
            serverResponse = serverResponseSerializeConvert.deserialize(response.getData());
            deCodeResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deCodeResponse() {
        ResponseCode code = serverResponse.getCode();

        switch (code) {
            case START_CONNECTION:
                me = serverResponse.getUser();

                System.out.println(me);
                System.out.println(serverResponse.getMessage());
                break;
            case END_CONNECTION:
            case KEEP_CONNECTION:
                System.out.println(serverResponse.getMessage());
                break;
            case END_CHAT_ROOM:
                System.out.println("You leaved the room");
                break;
            case START_CHAT_ROOM:
                startChatRoom();
                break;
            case ALL_ROOMS: {
                List<ChatRoom> rooms = serverResponse.getRoomList();
                System.out.println("-----------All Rooms-----------");
                System.out.println(rooms);
                break;
            }
            case MEMBERS: {
                ChatRoom room = serverResponse.getChatRoom();
                System.out.println("-------------------------------");
                System.out.println("Members: " + room.getMembers());
                System.out.println("-------------------------------");
                break;
            }
            default:
                System.out.println("Unknown response");
                break;
        }
    }

    private static void startChatRoom() {
        try {
            MulticastSocket multicastSocket = new MulticastSocket(ConnectionConfigurations.MULTICAST_SERVER_PORT);

            actualRoom = serverResponse.getChatRoom();
            InetAddress multicastAddress = actualRoom.getAddress();

            multicastSocket.joinGroup(multicastAddress);
            System.out.println("-----------" + actualRoom.getName().toUpperCase() + "-----------");
            System.out.println("Members: " + actualRoom.getMembers());
            System.out.println("------------------------------");

            String message;
            actualRoom.listenRoom(multicastSocket);

            boolean exit = false;
            do {
                message = read.nextLine();

                if (message.charAt(0) != '/') {
                    byte[] bytes = chatMessageSerializeConvert.serialize(new ChatMessage(me, message));

                    DatagramPacket messageOut = new DatagramPacket(bytes, bytes.length, multicastAddress, ConnectionConfigurations.MULTICAST_SERVER_PORT);
                    multicastSocket.send(messageOut);
                } else {
                    String[] splitedMessage = message.split(" ", 2);

                    String command = splitedMessage[0];

                    switch (command) {
                        case Commands.leave:
                            actualRoom.stopListeningRoom();

                            byte[] bytes = chatMessageSerializeConvert.serialize(new ChatMessage(me, "I'm leaving"));

                            DatagramPacket messageOut = new DatagramPacket(bytes, bytes.length, multicastAddress, ConnectionConfigurations.MULTICAST_SERVER_PORT);
                            multicastSocket.send(messageOut);
                            multicastSocket.leaveGroup(multicastAddress);

                            String leaveCommand = command + " " + actualRoom.getName() + " " + myNickname;
                            request = new DatagramPacket(leaveCommand.getBytes(), leaveCommand.length(), myAddress, ConnectionConfigurations.SERVER_PORT);
                            sendDataToServer();

                            exit = true;
                            break;
                        case Commands.members:
                            String getMembers = command + " " + actualRoom.getName();
                            request = new DatagramPacket(getMembers.getBytes(), getMembers.length(), myAddress, ConnectionConfigurations.SERVER_PORT);

                            sendDataToServer();
                            break;
                        default:
                            System.out.println("Command not found");
                            break;
                    }
                }
            } while (!exit);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
