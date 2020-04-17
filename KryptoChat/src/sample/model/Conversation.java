package sample.model;

import java.util.ArrayList;
import java.util.List;

public class Conversation
{
    protected List<Message> msgs;
    protected String client1id;
    protected String client2id;
    protected String typeOfEncryption;

    // No-Arg Constructor
    public Conversation()
    {
        this.msgs = new ArrayList<>();
        this.client1id = "";
        this.client2id = "";
        this.typeOfEncryption = "";
    }

    // Arg Constructor
    public Conversation(List<Message> msgs,
                        String client1id,
                        String client2id,
                        String typeOfEncryption)
    {
        this.msgs = msgs;
        this.client1id = client1id;
        this.client2id = client2id;
        this.typeOfEncryption = typeOfEncryption;
    }

    // Add a new message to the list
    public void add(Message m){
        this.msgs.add(m);
    }

    // Returns client1id
    public String getClient1id() {
        return client1id;
    }
    // Set client1id
    public void setClient1id(String client1id) {
        this.client1id = client1id;
    }

    // Returns client2id
    public String getClient2id() {
        return client2id;
    }
    // Set client1id
    public void setClient2id(String client2id) {
        this.client2id = client2id;
    }
    // Returns client1id
    public String getTypeOfEncryption() {
        return typeOfEncryption;
    }
    // Set client1id
    public void setTypeOfEncryption(String typeOfEncryption) {
        this.typeOfEncryption = typeOfEncryption;
    }
}
