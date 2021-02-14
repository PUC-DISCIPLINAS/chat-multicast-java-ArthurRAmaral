import enums.ResponseCode;
import utils.*;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Scanner;

public class MultCastMessageClient {
    static final int SERVER_PORT = ConnectionConfigurations.SERVER_PORT;
    static final int DEFAULT_BUFFER_SIZE = ConnectionConfigurations.DEFAULT_BUFFER_SIZE;
    static final int MULTICAST_SERVER_PORT = ConnectionConfigurations.MULTICAST_SERVER_PORT;
    static final SerializeConvert<ServerResponse> serverResponseSerializeConvert = new SerializeConvert<>();
    static final SerializeConvert<ChatMessage> chatMessageSerializeConvert = new SerializeConvert<>();
    static final Scanner read = new Scanner(System.in);
    static final String startCommand = "/start";
    static String myNickname = "";
    static InetAddress myAddress;

    public static void main(String[] args) {
        final int MULTICAST_SERVER_PORT = ConnectionConfigurations.MULTICAST_SERVER_PORT;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];


        DatagramPacket request;
        ServerResponse serverResponse;
        DatagramPacket reply;
        try (DatagramSocket aSocket = new DatagramSocket(); MulticastSocket mSocket = new MulticastSocket(MULTICAST_SERVER_PORT)) {

            myAddress = InetAddress.getByName(args[0]);

            System.out.print("Inser your name: ");
            myNickname = read.next();
            String initialCommand = startCommand + " " + myNickname;

            request = new DatagramPacket(initialCommand.getBytes(), initialCommand.length(), myAddress, SERVER_PORT);
            aSocket.send(request);


            reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);

            serverResponse = serverResponseSerializeConvert.deserialize(reply.getData());

            if (serverResponse.getCode() != ResponseCode.START_CONNECTION) {
                return;
            }

            System.out.println(serverResponse.getMensagem() + "\n\n");
            do {
                String comand = read.nextLine();

                comand += " " + myNickname;

                request = new DatagramPacket(comand.getBytes(), comand.length(), myAddress, SERVER_PORT);
                aSocket.send(request);

                reply = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply);

                serverResponse = serverResponseSerializeConvert.deserialize(reply.getData());

                deCodeResponse(serverResponse, mSocket);
            } while (serverResponse.getCode() != ResponseCode.END_CONNECTION);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void deCodeResponse(ServerResponse serverResponse, MulticastSocket mSocket) throws IOException {
        ResponseCode code = serverResponse.getCode();

        switch (code) {
            case START_CONNECTION:
            case END_CONNECTION:
            case KEEP_CONNECTION:
                System.out.println(serverResponse.getMensagem());
                break;
            case ROOM_ID: {
                ChatRoom room = (ChatRoom) serverResponse.getBody();
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
                boolean exit = true;
                do {
                    message = read.nextLine();

                    if (!message.equals("/exit")) {


                        byte[] bytes = chatMessageSerializeConvert.serialize(new ChatMessage(me, message));

                        DatagramPacket messageOut = new DatagramPacket(bytes, bytes.length, multicastAddress, MULTICAST_SERVER_PORT);
                        mSocket.send(messageOut);
                    } else {
                        exit = false;
                    }
                } while (exit);

            }
            case ALL_ROOMS: {
                List<ChatRoom> rooms = (List<ChatRoom>) serverResponse.getBody();
                System.out.println("-----------All Rooms-----------");
                System.out.println("Members: " + rooms);
                break;
            }
            default:
                System.out.println("Resposta desconhecida");
                break;
        }
    }
}
