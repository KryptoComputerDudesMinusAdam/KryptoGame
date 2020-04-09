package sample.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// every message sent and received is of this format
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    public static String contacts = "contacts";
    public static String clientName = "clientName";
    public static String conversationInvite = "conversationInvite";
    public static String conversationAccept = "conversationAccept";
    public static String conversationDecline = "conversationDecline";

    public String encryptedMessage;
    public int cypherOption;
    public String typeOfMessage;

    public Message(String encryptedMessage, int cypherOption){ // meant for messages to be encrypted/decrypted
        this.encryptedMessage = encryptedMessage;
        this.cypherOption = cypherOption;
    }

    public Message(String e){ // meant for messages sent straight to server for connection setup and instructions
        this.encryptedMessage = e;
        this.cypherOption = -1;
    }
}


