package sample.model;

import java.util.List;

public class MessageList{
    public String typeOfMessage;
    public List<Message> messages;

    public MessageList(List<Message> messages, String typeOfMessage){
        this.messages = messages;
        this.typeOfMessage = typeOfMessage;
    }
}
