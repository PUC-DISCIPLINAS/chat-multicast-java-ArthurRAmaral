import enums.ResponseCode;
import utils.*;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Scanner;

public class MultCastMessageClient {
    static final SerializeConvert<ServerResponse> serverResponseSerializeConvert = new SerializeConvert<>();
    static final SerializeConvert<ChatMessage> chatMessageSerializeConvert = new SerializeConvert<>();
    static final Scanner read = new Scanner(System.in);
    static final String startCommand = "/start";
    static final String leaveCommand = "/leave";
    static String myNickname = "";
    static InetAddress myAddress;

    public static void main(String[] args) {
        byte[] buffer = new byte[ConnectionConfigurations.DEFAULT_BUFFER_SIZE];

        DatagramPacket request;
        ServerResponse serverResponse;
        DatagramPacket reply;

        try (DatagramSocket aSocket = new DatagramSocket()) {
            myAddress = InetAddress.getByName(args[0]);

            System.out.print("Inser your name: ");
            myNickname = read.next();
            String initialCommand = startCommand + " " + myNickname;

            request = new DatagramPacket(initialCommand.getBytes(), initialCommand.length(), myAddress, ConnectionConfigurations.SERVER_PORT);
            aSocket.send(request);


            reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);

            serverResponse = serverResponseSerializeConvert.deserialize(reply.getData());

            if (serverResponse.getCode() != ResponseCode.START_CONNECTION) {
                return;
            }

            System.out.println(serverResponse.getMensagem() + "\n\n");

            read.nextLine();

            do {
                String comand = read.nextLine();

                comand += " " + myNickname;

                request = new DatagramPacket(comand.getBytes(), comand.length(), myAddress, ConnectionConfigurations.SERVER_PORT);
                aSocket.send(request);

                reply = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply);

                serverResponse = serverResponseSerializeConvert.deserialize(reply.getData());

                deCodeResponse(serverResponse, aSocket);
            } while (serverResponse.getCode() != ResponseCode.END_CONNECTION);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void deCodeResponse(ServerResponse serverResponse, DatagramSocket aSocket) throws IOException {
        ResponseCode code = serverResponse.getCode();

        switch (code) {
            case START_CONNECTION:
            case END_CONNECTION:
            case KEEP_CONNECTION:
                System.out.println(serverResponse.getMensagem());
                break;
            case END_CHAT_ROOM:
                System.out.println("You leaved the room");
                break;
            case START_CHAT_ROOM: {
                MulticastSocket mSocket = new MulticastSocket(ConnectionConfigurations.MULTICAST_SERVER_PORT);

                ChatRoom room = serverResponse.getChatRoom();
                mSocket.joinGroup(room.getAddress());
                System.out.println("-----------" + room.getName().toUpperCase() + "-----------");
                System.out.println("Members: " + room.getMembers());
                System.out.println("----------------------------------------------------------");

                InetAddress multicastAddress = room.getAddress();
                String message;
                room.listenRoom(mSocket);

                User me = null;
                for (User member : room.getMembers()) {
                    if (member.equals(new User(myAddress, myNickname))) {
                        me = member;
                        break;
                    }
                }
                boolean exit = false;
                do {
                    message = read.nextLine();

                    if (!message.equals(leaveCommand)) {
                        byte[] bytes = chatMessageSerializeConvert.serialize(new ChatMessage(me, message));

                        DatagramPacket messageOut = new DatagramPacket(bytes, bytes.length, multicastAddress, ConnectionConfigurations.MULTICAST_SERVER_PORT);
                        mSocket.send(messageOut);
                    } else {
                        byte[] bytes = chatMessageSerializeConvert.serialize(new ChatMessage(me, "I'm leaving"));

                        DatagramPacket messageOut = new DatagramPacket(bytes, bytes.length, multicastAddress, ConnectionConfigurations.MULTICAST_SERVER_PORT);
                        mSocket.send(messageOut);

                        DatagramPacket leaveRequest = new DatagramPacket(leaveCommand.getBytes(), leaveCommand.length(), myAddress, ConnectionConfigurations.SERVER_PORT);
                        aSocket.send(leaveRequest);

                        exit = true;
                    }
                } while (!exit);
            }
            break;
            case ALL_ROOMS: {
                List<ChatRoom> rooms = serverResponse.getRoomList();
                System.out.println("-----------All Rooms-----------");
                System.out.println(rooms);
                break;
            }
            default:
                System.out.println("Resposta desconhecida");
                break;
        }
    }
}
