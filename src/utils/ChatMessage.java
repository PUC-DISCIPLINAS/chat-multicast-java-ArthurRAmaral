package utils;

import java.io.Serializable;

    public class ChatMessage implements Serializable {
    final private User owner;
    final private String text;

    public ChatMessage(User owner, String text) {
        this.owner = owner;
        this.text = text;
    }

    public User getOwner() {
        return owner;
    }

    public String getText() {
        return text;
    }
}
