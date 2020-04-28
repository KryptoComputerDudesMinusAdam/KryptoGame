package sample.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import sample.model.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ServerController {

    @FXML
    Button connectButton, disconnectButton;
    @FXML
    TextField portTextField;
    @FXML
    ListView<Message> listView;
    private final List<Message> messages = new ArrayList<>();

    public void handleConnectButton(ActionEvent event) {
        // initialize the list view
        Controller.initializeListView(messages, listView);

        // start up the server thread
        if (portTextField.getText() != null &&
                portTextField.getText() != "") {
            int port = Integer.parseInt(portTextField.getText());
            ServerThread serverThread = new ServerThread(this);
            serverThread.port = port;
            serverThread.start();
        }
    }

    public void handleDisconnectButton(ActionEvent event) {
        displayNewMessage(new Message("Closing server down"));
    }


    public void displayNewMessage(Message m) {
        Platform.runLater(() -> {
            messages.add(m);
            listView.getItems().setAll(messages);
            listView.refresh();
        });
    }
}

// represents the server
class ServerThread extends Thread {
    int port;
    private final ServerController serverController;
    static List<ServerClientThread> clients = new ArrayList<>();
    static HashMap<String, String> connections = new HashMap<>();
    public static List<Conversation> conversations = new ArrayList<>();


    ServerThread(ServerController serverController) {
        this.serverController = serverController;
    }

    public void run() {
        try {
            // create a server socket
            ServerSocket serverSocket = new ServerSocket(port);

            // continuously look for and add connections
            while (true) {
                serverController.displayNewMessage(new Message("Server is open for connection..."));
                Socket socket = serverSocket.accept();

                serverController.displayNewMessage(new Message("Received connection from port " + socket.getPort()));
                ServerClientThread sct = new ServerClientThread(socket, serverController, this);
                sct.start();
                ServerThread.clients.add(sct);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }

    void removeClient(ServerClientThread sct) {
        ServerThread.clients.remove(sct);
    }

    void broadcastContactsList() {
        // send message to client: contacts list
        ServerThread.clients.forEach(cl -> {
            for (ServerClientThread sct : ServerThread.clients) {
                try {
                    Message contact = new Message(sct.id);
                    contact.typeOfMessage = Message.contacts;
                    cl.objectOutputStream.writeObject(contact);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

// represents a single connection to a client
class ServerClientThread extends Thread {
    private ServerController serverController;
    private ServerThread serverThread;
    private Socket socket;
    ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    String id;
    private String name;
    private int port;
    private String typeOfCipher;
    private boolean foundConversation = false;
    private Conversation conversation;
    private ServerClientThread client2;
    RSAGenerator keyPairGenerator;


    ServerClientThread(Socket socket, ServerController serverController, ServerThread serverThread) throws NoSuchAlgorithmException {
        this.socket = socket;
        this.serverController = serverController;
        this.serverThread = serverThread;
        this.port = socket.getPort();
        this.conversation = new Conversation();
        this.keyPairGenerator = new RSAGenerator();
    }

    public void run() {
        try {
            // for writing and reading to sockets
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

            // get first message from client: detect client vs attacker
            Message m = (Message) objectInputStream.readObject();
            name = m.from;

            // start attacker or client logic
            if (name.startsWith("Attacker")) {
                handleAttacker(name.substring(8));
            } else {
                handleClient();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(){
        try {
            // give client their unique ID and broadcast new contacts list
            Message uniqueIDMessage = new Message(id = name + "#" + port);
            uniqueIDMessage.typeOfMessage = Message.uniqueID;
            objectOutputStream.writeObject(uniqueIDMessage);
            serverController.displayNewMessage(new Message("Added " + id));
            serverThread.broadcastContactsList();

            // continuously check for messages coming from client
            new Thread(() -> {
                try {
                    // try to get two clients to connect to each other
                    while (true) {
                        System.out.println("Updated conversation object: " + conversation.msgs.toString());
                        Message message = (Message) objectInputStream.readObject();
                        // client still needs to connect to another client
                        if (!foundConversation) {
                            waitForConversation(message);
                        } else {
                            handleConversation(message);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForConversation(Message message){
        try {
            for (ServerClientThread cli : ServerThread.clients) {
                if (cli.id.equals(message.to)) {
                    // initialize message to pass over to other clients
                    System.out.println("found match");
                    Message messageToOtherClient = new Message();
                    messageToOtherClient.from = id;
                    messageToOtherClient.typeOfCipher = message.typeOfCipher;
                    messageToOtherClient.typeOfMessage = message.typeOfMessage;
                    cli.objectOutputStream.writeObject(messageToOtherClient);

                    // connect the two clients together
                    if (messageToOtherClient.typeOfMessage.equals(Message.conversationAccept)) {
                        ServerThread.conversations.add(conversation);
                        client2 = cli;
                        client2.id = message.to;
                        ServerThread.connections.put(id, client2.id);
                        ServerThread.connections.put(client2.id, id);
                        conversation.setClient1id(id);
                        conversation.setClient2id(client2.id);
                        serverController.displayNewMessage(new Message("New conversation: " + id + " and " + ServerThread.connections.get(id)));
                        foundConversation = true;
                        client2.foundConversation = true;

                        // *
                        typeOfCipher = message.typeOfCipher;
                        System.out.println("Setting conversation: " + conversation.getClient1id() + conversation.getClient2id() + "\n\twith: " + conversation.getPublicKey());
                        int index = ServerThread.conversations.indexOf(conversation);
                        System.out.println("Checking static: "+ index + " here: "+ServerThread.conversations.get(index).getPublicKey());
                        client2.typeOfCipher = message.typeOfCipher;
                        String key;
                        switch (typeOfCipher) {
                            case Message.cipherMonoAlphabetic:
                                key = Cipher.generateMonoKey();
                                break;
                            case Message.cipherVigenere:
                                key = Cipher.generateBasicKey();
                                break;
                            case Message.cipherStream:
                                key = Cipher.generateBasicKey();
                                break;
                            case Message.cipherRSA:
                                key = Base64.getEncoder().encodeToString(keyPairGenerator.getPublicKey().getEncoded());
                                System.out.println("PUBLIC: " + key);
                                break;
                            default:
                                key = Cipher.generateMonoKey();
                                break;
                        }
                        conversation.setPublicKey(key);
                        client2.conversation.setPublicKey(key);
                        conversation.setTypeOfEncryption(typeOfCipher);
                        Message publicKeyMessage = new Message(key);
                        publicKeyMessage.typeOfCipher = typeOfCipher;
                        publicKeyMessage.typeOfMessage = Message.conversationPublicKey;
                        if(typeOfCipher.equals(Message.cipherRSA)){
                            client2.objectOutputStream.writeObject(publicKeyMessage);
                            Message publicKeyMessage2 = new Message(Base64.getEncoder().encodeToString(client2.keyPairGenerator.getPublicKey().getEncoded()));
                            publicKeyMessage2.typeOfCipher = typeOfCipher;
                            publicKeyMessage2.typeOfMessage = Message.conversationPublicKey;
                            objectOutputStream.writeObject(publicKeyMessage2);
                        } else{
                            objectOutputStream.writeObject(publicKeyMessage);
                            client2.objectOutputStream.writeObject(publicKeyMessage);
                        }
                        serverController.displayNewMessage(new Message("Generated public key:\n\t " + publicKeyMessage.message));

                        // generate potential private key for RSA
                        if(typeOfCipher.equals(Message.cipherRSA)){
                            String privateKey1 = Base64.getEncoder().encodeToString(keyPairGenerator.getPrivateKey().getEncoded());
                            Message privateKeyMessage1 = new Message(privateKey1);
                            privateKeyMessage1.typeOfCipher = typeOfCipher;
                            privateKeyMessage1.typeOfMessage = Message.conversationPrivateKey;
                            objectOutputStream.writeObject(privateKeyMessage1);
                            serverController.displayNewMessage(new Message("Generated private key ending in:\n\t " + privateKeyMessage1.message.substring(privateKeyMessage1.message.length()-5)));
                            System.out.println("PRIVATE 1: " + privateKeyMessage1.message);

                            String privateKey2 = Base64.getEncoder().encodeToString(client2.keyPairGenerator.getPrivateKey().getEncoded());
                            Message privateKeyMessage2 = new Message(privateKey2);
                            privateKeyMessage2.typeOfCipher = typeOfCipher;
                            privateKeyMessage2.typeOfMessage = Message.conversationPrivateKey;
                            client2.objectOutputStream.writeObject(privateKeyMessage2);
                            serverController.displayNewMessage(new Message("Generated private key ending in:\n\t " + privateKeyMessage2.message.substring(privateKeyMessage2.message.length()-5)));
                            System.out.println("PRIVATE 2: " + privateKeyMessage2.message);
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConversation(Message message){
        try {
            conversation.msgs.add(message);
            conversation.setTypeOfEncryption(typeOfCipher);
            for (ServerClientThread client : ServerThread.clients) {
                if (client.id.equals(ServerThread.connections.get(id))) {
                    System.out.println("Writing message: " + message.message + "\n\tFrom: " + id + " to " + ServerThread.connections.get(id));
                    client2 = client;
                    client2.objectOutputStream.writeObject(message);
                    client2.conversation.msgs.add(message);
                    client2.conversation.setTypeOfEncryption(typeOfCipher);
                    client2.conversation.setClient1id(client2.id);
                    client2.conversation.setClient2id(id);
                    serverController.displayNewMessage(new Message("Transferring encrypted message: " + message.message + "\n\tEncryption Type: " + typeOfCipher));
                    if (message.typeOfMessage.equals(Message.terminate)) {
                        serverController.displayNewMessage(new Message(id + " and " + client2.id + " ended their conversation."));
                        serverThread.removeClient(this);
                        serverThread.removeClient(client2);
                        ServerThread.connections.remove(id);
                        ServerThread.connections.remove(client2.id);
                        serverController.displayNewMessage(new Message("Closing socket " + socket.getPort()));
                        socket.close();
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAttacker(String clientName) throws IOException {
        System.out.println("In Switch: " + clientName);
        Conversation cs = findCurrentCon();
        if(cs != null){
            Message m;
            switch (clientName) {
                case "CiphertextOnly":
                    objectOutputStream.writeObject(cs);
                    break;
                case "Known-Plaintext":
                    int max;
                    if(cs.msgs.size() >= 5){
                        max = 5;
                    } else{
                        max = cs.msgs.size();
                    }
                    System.out.println("index: "+max);

                    for(int i = 0; i<max; i++){
                        objectOutputStream.writeObject(cs.msgs.get(i));
                        switch(cs.typeOfEncryption){
                            case Message.cipherMonoAlphabetic:
                                m = new Message(Cipher.monoalphabeticDec(cs.getPublicKey(), cs.msgs.get(i).message));
                                break;
                            case Message.cipherVigenere:
                                m = new Message(Cipher.vigenereDec(cs.getPublicKey(), cs.msgs.get(i).message));
                                break;
                            case Message.cipherStream:
                                m = new Message(Cipher.streamDec(cs.getPublicKey(), cs.msgs.get(i).message));
                                break;
                            default:
                                m = new Message();
                                break;
                        }
                        m.isEncrypted = false;
                        objectOutputStream.writeObject(m);
                    }
                    break;
                case "ChoseCiphertext":
                    System.out.println("Chosen Ciphertext");
                    System.out.println("Key: "+cs.getPublicKey());

                    try {
                        while(true) {
                            // check for incoming plain texts to encrypt and send back
                            m = (Message) objectInputStream.readObject();
                            String str = null;
                            Message output;
                            switch (cs.typeOfEncryption) {
                                case Message.cipherMonoAlphabetic:
                                    str = Cipher.monoalphabeticDec(cs.getPublicKey(), m.message);
                                    break;
                                case Message.cipherVigenere:
                                    str = Cipher.vigenereDec(cs.getPublicKey(), m.message);
                                    break;
                                case Message.cipherStream:
                                    str = Cipher.streamDec(cs.getPublicKey(), m.message);
                                    break;
                            }
                            //send message enc out
                            output = new Message(str);
                            output.isEncrypted = true;
                            output.typeOfCipher = cs.typeOfEncryption;
                            objectOutputStream.writeObject(output);
                            serverController.displayNewMessage(new Message("Sending out encrypted message to attacker:\n\t" + output.message));
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                case "ChosePlaintext":
                    System.out.println("Chosen Plaintext");
                    System.out.println("Key: "+cs.getPublicKey());

                    try {
                        while(true) {
                            // check for incoming plain texts to encrypt and send back
                            m = (Message) objectInputStream.readObject();
                            System.out.println("MESSAGE: "+m.isEncrypted + m.message + m.typeOfCipher + m.from + m.to);
                            String str = null;
                            Message output;
                            switch (cs.typeOfEncryption) {
                                case Message.cipherMonoAlphabetic:
                                    str = Cipher.monoalphabeticEnc(cs.getPublicKey(), m.message);
                                    break;
                                case Message.cipherVigenere:
                                    str = Cipher.vigenereEnc(cs.getPublicKey(), m.message);
                                    break;
                                case Message.cipherStream:
                                    str = Cipher.streamEnc(cs.getPublicKey(), m.message);
                                    break;
                            }
                            //send message enc out
                            output = new Message(str);
                            output.isEncrypted = true;
                            output.typeOfCipher = cs.typeOfEncryption;
                            objectOutputStream.writeObject(output);
                            serverController.displayNewMessage(new Message("Sending out encrypted message to attacker:\n\t" + output.message));
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
            }
        }else{
            switch (clientName)
            {
                case "CiphertextOnly":
                    objectOutputStream.writeObject(cs);
                    break;
            }
        }
    }

    private Conversation findCurrentCon() {
        System.out.println("Size: "+ServerThread.conversations.size());
        if(ServerThread.conversations.size() > 0){
            int randomIndex = ThreadLocalRandom.current().nextInt(0, ServerThread.conversations.size());
            System.out.println("returning index: "+randomIndex);
            Conversation conv = ServerThread.conversations.get(randomIndex);
            System.out.println("Returning key: " + conv.getPublicKey() + conv.getClient1id() + conv.getClient2id());
            return conv;
        } else{
            return null;
        }
    }
}