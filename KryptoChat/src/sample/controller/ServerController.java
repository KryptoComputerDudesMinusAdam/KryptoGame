package sample.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import sample.model.Cipher;
import sample.model.Conversation;
import sample.model.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    public static List<Conversation> allCon = new ArrayList<>();

    public void handleConnectButton(ActionEvent event) {
        // initialize the list view
        Controller.initializeListView(messages, listView);

        // start up the server thread
        if (portTextField.getText() != null) {
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
        } catch (IOException e) {
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
                    Message contact = new Message(sct.clientId);
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
    private final ServerController serverController;
    private final ServerThread serverThread;
    private final Socket socket;
    Conversation conversation;
    private final int clientPort;
    String clientId;
    String client2Id;
    ServerClientThread client2;
    ObjectOutputStream objectOutputStream;
    private boolean foundConversation = false;
    private ObjectInputStream objectInputStream;


    // *
    public String typeOfCipher;

    ServerClientThread(Socket socket, ServerController serverController, ServerThread serverThread) {
        this.socket = socket;
        this.serverController = serverController;
        this.serverThread = serverThread;
        this.clientPort = socket.getPort();
        this.conversation = new Conversation();
        ServerController.allCon.add(this.conversation);
    }

    public void run() {

        try {
            // for writing and reading to sockets
            OutputStream outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            InputStream inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            // read message from client: client name and add to contacts list
            Message m = (Message) objectInputStream.readObject();
            String clientName = m.encryptedMessage;
            if (clientName.startsWith("Attacker")) {
                handelAttacker(objectOutputStream, objectInputStream, clientName.substring(8));
            } else {
                clientId = clientName + "#" + clientPort;
                Message uniqueIDMessage = new Message(clientId);
                uniqueIDMessage.typeOfMessage = Message.uniqueID;
                objectOutputStream.writeObject(uniqueIDMessage);

                serverController.displayNewMessage(new Message("Added " + clientId));
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
                                for (ServerClientThread client : ServerThread.clients) {
                                    if (client.clientId.equals(message.encryptedMessage)) {
                                        // *
                                        Message messageToOtherClient = new Message(clientId, message.typeOfCipher);
                                        messageToOtherClient.typeOfMessage = message.typeOfMessage;
                                        client.objectOutputStream.writeObject(messageToOtherClient);

                                        // connect the two clients together
                                        if (messageToOtherClient.typeOfMessage.equals(Message.conversationAccept)) {
                                            client2 = client;
                                            client2Id = message.encryptedMessage;
                                            ServerThread.connections.put(clientId, client2Id);
                                            ServerThread.connections.put(client2Id, clientId);
                                            this.conversation.setClient1id(clientId);
                                            this.conversation.setClient2id(client2Id);
                                            serverController.displayNewMessage(new Message("New conversation: " + clientId + " and " + ServerThread.connections.get(clientId)));
                                            foundConversation = true;
                                            client.foundConversation = true;

                                            // *
                                            typeOfCipher = message.typeOfCipher;
                                            System.out.println("Setting conversation: "+conversation.getClient1id()+conversation.getClient2id()+"\n\twith: "+conversation.getPublicKey());
                                            int index = ServerController.allCon.indexOf(conversation);
                                            System.out.println("Checking static: "+ index + " here: "+ServerController.allCon.get(index).getPublicKey());
                                            client.typeOfCipher = message.typeOfCipher;
                                            String key;
                                            switch (this.typeOfCipher) {
                                                case Message.cipherMonoAlphabetic:
                                                    key = Cipher.generateMonoKey();
                                                    break;
                                                case Message.cipherVigenere:
                                                    key = Cipher.generateBasicKey();
                                                    break;
                                                case Message.cipherStream:
                                                    key = Cipher.generateBasicKey();
                                                    break;
                                                default:
                                                    key = Cipher.generateMonoKey();
                                                    break;
                                            }
                                            this.conversation.setPublicKey(key);
                                            client2.conversation.setPublicKey(key);
                                            conversation.setTypeOfEncryption(this.typeOfCipher);
                                            Message keyMessage = new Message(key);
                                            keyMessage.typeOfCipher = this.typeOfCipher;
                                            keyMessage.typeOfMessage = Message.conversationKey;
                                            objectOutputStream.writeObject(keyMessage);
                                            client.objectOutputStream.writeObject(keyMessage);
                                            serverController.displayNewMessage(new Message("Generated secret key " + keyMessage.encryptedMessage));
                                        }
                                        break;
                                    }
                                }
                            } else {
                                this.conversation.msgs.add(message);
                                this.conversation.setTypeOfEncryption(this.typeOfCipher);
                                for (ServerClientThread client : ServerThread.clients) {
                                    if (client.clientId.equals(ServerThread.connections.get(clientId))) {
                                        client2 = client;
                                        break;
                                    }
                                }
                                System.out.println("Writing message: " + message.encryptedMessage + "\n\tFrom: " + clientId + " to " + ServerThread.connections.get(clientId));
                                client2.objectOutputStream.writeObject(message);
                                client2.conversation.msgs.add(message);
                                client2.conversation.setTypeOfEncryption(this.typeOfCipher);
                                client2.conversation.setClient1id(client2.clientId);
                                client2.conversation.setClient2id(clientId);
                                if (message.typeOfMessage.equals(Message.terminate)) {
                                    serverController.displayNewMessage(new Message(clientId + " and " + client2.clientId + " ended their conversation."));
                                    serverThread.removeClient(this);
                                    serverThread.removeClient(client2);
                                    ServerThread.connections.remove(clientId);
                                    ServerThread.connections.remove(client2.clientId);
                                    serverController.displayNewMessage(new Message("Closing socket " + socket.getPort()));
                                    socket.close();
                                }
                                serverController.displayNewMessage(new Message("Transferring encrypted message: " + message.encryptedMessage + "\n\tEncryption Type: " + message.typeOfCipher));
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                }).start();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handelAttacker(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, String clientName) throws IOException {
        System.out.println("In Switch: " + clientName);
        Conversation cs;
        switch (clientName) {
            case "CiphertextOnly":
                cs = findCurrentCon();
                objectOutputStream.writeObject(cs);
                break;
            case "KnownPlaintext":
                break;
            case "ChoseCiphertext":
                System.out.println("Chosen Ciphertext");
                cs = findCurrentCon();
                System.out.println("Key: "+cs.getPublicKey());
                while(true) {
                    try {
                        // check for incoming plain texts to encrypt and send back
                        Message m = (Message) objectInputStream.readObject();
                        String str = null;
                        Message output;
                        switch(cs.typeOfEncryption){
                            case Message.cipherMonoAlphabetic:
                                str = Cipher.monoalphabeticDec(cs.getPublicKey(), m.encryptedMessage);
                                break;
                            case Message.cipherVigenere:
                                str = Cipher.vigenereDec(cs.getPublicKey(), m.encryptedMessage);
                                break;
                            case Message.cipherStream:
                                str = Cipher.streamDec(cs.getPublicKey(), m.encryptedMessage);
                                break;
                        }
                        //send message enc out
                        output = new Message(str);
                        output.isEncrypted = true;
                        output.typeOfCipher = cs.typeOfEncryption;
                        objectOutputStream.writeObject(output);
                        serverController.displayNewMessage(new Message("Sending out encrypted message to attacker:\n\t"+output.encryptedMessage));
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            case "ChosePlaintext":
                System.out.println("Chosen Plaintext");
                cs = findCurrentCon();
                System.out.println("Key: "+cs.getPublicKey());
                while(true) {
                    try {
                        // check for incoming plain texts to encrypt and send back
                        Message m = (Message) objectInputStream.readObject();
                        String str = null;
                        Message output;
                        switch(cs.typeOfEncryption){
                            case Message.cipherMonoAlphabetic:
                                str = Cipher.monoalphabeticEnc(cs.getPublicKey(), m.encryptedMessage);
                                break;
                            case Message.cipherVigenere:
                                str = Cipher.vigenereEnc(cs.getPublicKey(), m.encryptedMessage);
                                break;
                            case Message.cipherStream:
                                str = Cipher.streamEnc(cs.getPublicKey(), m.encryptedMessage);
                                break;
                        }
                        //send message enc out
                        output = new Message(str);
                        output.isEncrypted = true;
                        output.typeOfCipher = cs.typeOfEncryption;
                        objectOutputStream.writeObject(output);
                        serverController.displayNewMessage(new Message("Sending out encrypted message to attacker:\n\t"+output.encryptedMessage));
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private Conversation findCurrentCon() {
        int randomIndex = ThreadLocalRandom.current().nextInt(0, ServerController.allCon.size()-1);
        System.out.println("returning index: "+randomIndex);
        Conversation conv = ServerController.allCon.get(randomIndex);
        System.out.println("Returning key: " + conv.getPublicKey() + conv.getClient1id() + conv.getClient2id());
        return conv;
    }
}