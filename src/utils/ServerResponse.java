package utils;

import enums.ResponseCode;

import java.io.Serializable;
import java.util.List;

public class ServerResponse implements Serializable {
    private ResponseCode code = ResponseCode.KEEP_CONNECTION;
    private String message = "";
    private ChatRoom chatRoom = null;
    private List<ChatRoom> roomList = null;
    private User user = null;

    public ServerResponse(String message) {
        this.message = message;
    }

    public ServerResponse(ResponseCode code) {
        this.code = code;
    }

    public ServerResponse(ResponseCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public ServerResponse(ResponseCode code, List<ChatRoom> roomList) {
        this.code = code;
        this.roomList = roomList;
    }

    public ServerResponse(ResponseCode code, ChatRoom chatRoom) {
        this.code = code;
        this.chatRoom = chatRoom;
    }

    public ServerResponse(ResponseCode code, String message, User user) {
        this.code = code;
        this.message = message;
        this.user = user;
    }

    public User getUser() {
        return user;
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

    public String getMessage() {
        return message;
    }
}
