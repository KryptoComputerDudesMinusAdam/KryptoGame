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
    public final static String conversationKey = "key";

    public String encryptedMessage;
    public int cypherOption;
    public String typeOfMessage;
    public boolean isEncrypted;

    public String typeOfCipher;
    public final static String cipherMonoAlphabetic = "monoalphabetic";
    public final static String cipherVigenere = "vigenere";
    public final static String cipherStream = "stream";



    public Message(String encryptedMessage, String typeOfCipher){ // meant for messages to be encrypted/decrypted
        this.encryptedMessage = encryptedMessage;
        this.typeOfCipher = typeOfCipher;
    }

    public Message(String e){ // meant for messages sent straight to server for connection setup and instructions
        this.encryptedMessage = e;
        this.cypherOption = -1;
    }
}


