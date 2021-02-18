package utils;

import enums.ResponseCode;

import java.io.Serializable;
import java.util.List;

public class ServerResponse implements Serializable {
    private ResponseCode code = ResponseCode.KEEP_CONNECTION;
    private String mensagem = "";
    private ChatRoom chatRoom = null;
    private List<ChatRoom> roomList = null;

    public ServerResponse(String mensagem) {
        this.mensagem = mensagem;
    }

    public ServerResponse(ResponseCode code) {
        this.code = code;
    }

    public ServerResponse(ResponseCode code, String mensagem) {
        this.code = code;
        this.mensagem = mensagem;
    }

    public ServerResponse(ResponseCode code, List<ChatRoom> roomList) {
        this.code = code;
        this.roomList = roomList;
    }

    public ServerResponse(ResponseCode code, ChatRoom chatRoom) {
        this.code = code;
        this.chatRoom = chatRoom;
    }

    public List<ChatRoom> getRoomList() {
        return roomList;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public ResponseCode getCode() {
        return code;
    }

    public String getMensagem() {
        return mensagem;
    }
}
