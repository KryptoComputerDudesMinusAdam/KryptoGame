package sample.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Conversation implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<Message> msgs;
    public String client1id;
    public String client2id;
    public String typeOfEncryption;
    public String publicKey;
    public String RSApublicKey;
    public String RSAprivateKey;

    // No-Arg Constructor
    public Conversation()
    {
        this.msgs = new ArrayList<>();
        this.client1id = "";
        this.client2id = "";
        this.typeOfEncryption = "";
    }

    public boolean isEmpty()
    {
        if(this.msgs == null
                && this.client1id.isEmpty()
                && this.client2id.isEmpty())
            return true;
        return false;
    }
    // Add a new message to the list
    public void add(Message m){
        this.msgs.add(m);
    }
    public void addMessageList(List<Message> m){this.msgs.addAll(m);}
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
    public String getPublicKey() {
        return publicKey;
    }
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getRSApublicKey() {
        return RSApublicKey;
    }
    public void setRSApublicKey(String RSApublicKey) {
        this.RSApublicKey = RSApublicKey;
    }
    public String getRSAprivateKey() {
        return RSAprivateKey;
    }
    public void setRSAprivateKey(String RSAprivateKey) {
        this.RSAprivateKey = RSAprivateKey;
    }
}
