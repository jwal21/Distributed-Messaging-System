package model;
//serializable allows Message objects to be sent over Object streams. 
import java.io.Serializable;

public class Message implements Serializable {
	//recommended serialVersionUID for Serializable classes. 
    private static final long serialVersionUID = 1L;
    //type of message (JOIN, LEAVE, PRIVATE_MESSAGE, etc.).
    private final MessageType type;
    //ID of the sender.
    private final String senderId;
    //main content of the message
    private final String content;

    //constructs a new message object.
    public Message(MessageType type, String senderId, String content) {
        this.type = type;
        this.senderId = senderId;
        this.content = content;
    }
    //returns a message type
    public MessageType getType() {
        return type;
    }
    //returns the sender ID
    public String getSenderId() {
        return senderId;
    }
    //returns the message content.
    public String getContent() {
        return content;
    }
}
