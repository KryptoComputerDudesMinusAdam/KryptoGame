package sample.model;

import java.io.Serializable;
import java.util.List;

public class MessageList implements Serializable{
    private static final long serialVersionUID = 1L;
    public String typeOfMessage;
    public List<Message> messages;

    public MessageList(List<Message> messages, String typeOfMessage){
        this.messages = messages;
        this.typeOfMessage = typeOfMessage;
    }
}
