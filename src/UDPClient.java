import enums.ResponseCode;
import utils.ConnectionConfigurations;
import utils.ServerResponse;
import utils.ServerResponseSerializeConvert;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class UDPClient {
    static final int SERVER_PORT = ConnectionConfigurations.SERVER_PORT;
    static final int DEFAULT_BUFFER_SIZE = ConnectionConfigurations.DEFAULT_BUFFER_SIZE;
    static final ServerResponseSerializeConvert serverResponseSerializeConvert = new ServerResponseSerializeConvert();
    static final Scanner read = new Scanner(System.in);
    static final String startCommand = "/start";

    public static void main(String[] args) {

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        String message;
        DatagramPacket request;
        ServerResponse serverResponse;
        DatagramPacket reply;
        try (DatagramSocket aSocket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(args[0]);

            request = new DatagramPacket(startCommand.getBytes(), startCommand.length(), address, SERVER_PORT);
            aSocket.send(request);


            reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);

            serverResponse = serverResponseSerializeConvert.deserialize(reply.getData());

            if (serverResponse.getCode() != ResponseCode.START_CONNECTION) {
                return;
            }

            do {
                System.out.println("Pronto para usar\n\n");
                String comand = read.nextLine();
                System.out.println(comand);

                request = new DatagramPacket(comand.getBytes(), comand.length(), address, SERVER_PORT);
                aSocket.send(request);


                reply = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply);

                serverResponse = serverResponseSerializeConvert.deserialize(reply.getData());
                message = serverResponse.getMensagem();

                System.out.println("Resposta: " + message);
            } while (serverResponse.getCode() != ResponseCode.END_CONNECTION);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
