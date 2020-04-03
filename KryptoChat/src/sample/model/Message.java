package sample.model;

import java.io.Serializable;

// every message sent and received is of this format
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    public String encryptedMessage;
    public int cypherOption;

    public Message(String encryptedMessage, int cypherOption){ // meant for messages to be encrypted/decrypted
        this.encryptedMessage = encryptedMessage;
        this.cypherOption = cypherOption;
    }

    public Message(String e){ // meant for messages sent straight to server for connection setup and instructions
        this.encryptedMessage = e;
        this.cypherOption = -1;
    }
}
