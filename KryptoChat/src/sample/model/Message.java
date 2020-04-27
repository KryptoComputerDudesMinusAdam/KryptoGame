package sample.model;

import java.io.Serializable;

// every message sent and received is of this format
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    public final static String contacts = "contacts";
    public final static String clientName = "clientName";
    public final static String conversationInvite = "conversationInvite";
    public final static String conversationAccept = "conversationAccept";
    public final static String conversationDecline = "conversationDecline";
    public final static String terminate = "terminate";
    public final static String conversationPublicKey = "conversationPublicKey";
    public final static String conversationPrivateKey = "conversationPrivateKey";
    public final static String uniqueID = "conversationUniqueID";
    public final static String simpleMessage = "simpleMessage";
    public final static String cipherMonoAlphabetic = "monoalphabetic";
    public final static String cipherVigenere = "vigenere";
    public final static String cipherStream = "stream";
    public final static String cipherRSA = "RSA";

    public String from;
    public String to;
    public String message;
    public String typeOfMessage;
    public boolean isEncrypted;
    public String typeOfCipher;

    public Message(String message, String typeOfCipher){ // meant for messages to be encrypted/decrypted
        this.message = message;
        this.typeOfCipher = typeOfCipher;
    }

    public Message(){ // misc constructor

    }

    public Message(String e){ // meant for messages sent straight to server for connection setup and instructions
        this.message = e;
    }
}


