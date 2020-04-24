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

public class ServerController {

    @FXML Button connectButton, disconnectButton;
    @FXML TextField portTextField;
    @FXML ListView<Message> listView;
    private List<Message> messages = new ArrayList<>();
    public static List<Conversation> allCon = new ArrayList<>();

    public void handleConnectButton(ActionEvent event){
        // initialize the list view
        Controller.initializeListView(messages, listView);

        // start up the server thread
        if(portTextField.getText() != null){
            int port = Integer.parseInt(portTextField.getText());
            ServerThread serverThread = new ServerThread(this);
            serverThread.port = port;
            serverThread.start();
        }
    }

    public void handleDisconnectButton(ActionEvent event){
        displayNewMessage(new Message("Closing server down"));
    }



    public void displayNewMessage(Message m){
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
    private ServerController serverController;
    static List<ServerClientThread> clients = new ArrayList<>();
    static HashMap<String, String> connections = new HashMap<>();

    ServerThread(ServerController serverController){
        this.serverController = serverController;
    }

    public void run(){
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

    void removeClient(ServerClientThread sct){
        ServerThread.clients.remove(sct);
    }

    void broadcastContactsList() {
        // send message to client: contacts list
        ServerThread.clients.forEach(cl -> {
            for (ServerClientThread sct: ServerThread.clients) {
                try {
                    Message contact = new Message(sct.clientId);
                    contact.typeOfMessage = Message.contacts;
                    cl.objectOutputStream.writeObject(contact);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }});
    }
}

// represents a single connection to a client
class ServerClientThread extends Thread {
    private ServerController serverController;
    private ServerThread serverThread;
    private Socket socket;
    Conversation conversation;
    private int clientPort;
    String clientId;
    String client2Id;
    ServerClientThread client2;
    ObjectOutputStream objectOutputStream;
    private boolean foundConversation = false;
    private ObjectInputStream objectInputStream;



    // *
    public String typeOfCipher;

    ServerClientThread(Socket socket, ServerController serverController, ServerThread serverThread){
        this.socket = socket;
        this.serverController = serverController;
        this.serverThread = serverThread;
        this.clientPort = socket.getPort();
        this.conversation = new Conversation();
        ServerController.allCon.add(this.conversation);
    }

    public void run(){

        try {
            // for writing and reading to sockets
            OutputStream outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            InputStream inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            // read message from client: client name and add to contacts list
            Message m = (Message) objectInputStream.readObject();
            String clientName = m.encryptedMessage;
            clientId = clientName +"#"+ clientPort;
            Message uniqueIDMessage = new Message(clientId);
            uniqueIDMessage.typeOfMessage = Message.uniqueID;
            objectOutputStream.writeObject(uniqueIDMessage);

            // Attacker
            if(clientName.startsWith("Attacker")){
                handelAttacker(objectOutputStream, objectInputStream, clientName.substring(8));
            }else{
                serverController.displayNewMessage(new Message("Added "+clientId));
                serverThread.broadcastContactsList();
            // continuously check for messages coming from client
            new Thread(()->{
                try {
                    // try to get two clients to connect to each other
                    while(true){
                        System.out.println("Updated conversation object: "+conversation.msgs.toString());
                        Message message = (Message) objectInputStream.readObject();
                        // client still needs to connect to another client
                        if(!foundConversation){
                            for(ServerClientThread client : ServerThread.clients){
                                if(client.clientId.equals(message.encryptedMessage)){
                                    // *
                                    Message messageToOtherClient = new Message(clientId, message.typeOfCipher);
                                    messageToOtherClient.typeOfMessage = message.typeOfMessage;
                                    client.objectOutputStream.writeObject(messageToOtherClient);

                                    // connect the two clients together
                                    if(messageToOtherClient.typeOfMessage.equals(Message.conversationAccept)){
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
                                        client.typeOfCipher = message.typeOfCipher;
                                        String key;
                                        switch(this.typeOfCipher) {
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
                                        Message keyMessage = new Message(key);
                                        keyMessage.typeOfCipher = this.typeOfCipher;
                                        keyMessage.typeOfMessage = Message.conversationKey;
                                        objectOutputStream.writeObject(keyMessage);
                                        client.objectOutputStream.writeObject(keyMessage);
                                    }
                                    break;
                                }
                            }
                        } else {
                            this.conversation.msgs.add(message);
                            this.conversation.setTypeOfEncryption(this.typeOfCipher);
                            for(ServerClientThread client : ServerThread.clients){
                                if(client.clientId.equals(ServerThread.connections.get(clientId))){
                                    client2 = client;
                                    break;
                                }
                            }
                            System.out.println("Writing message: "+message.encryptedMessage+"\n\tFrom: "+clientId + " to " + ServerThread.connections.get(clientId));
                            client2.objectOutputStream.writeObject(message);
                            client2.conversation.msgs.add(message);
                            if(message.typeOfMessage.equals(Message.terminate)){
                                serverController.displayNewMessage(new Message(clientId + " and " + client2.clientId + " ended their conversation."));
                                serverThread.removeClient(this);
                                serverThread.removeClient(client2);
                                ServerThread.connections.remove(clientId);
                                ServerThread.connections.remove(client2.clientId);
                                socket.close();
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            }).start();}
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handelAttacker(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, String clientName) throws IOException
    {
        System.out.println("In Switch: " + clientName);
        switch (clientName)
        {
            case "CiphertextOnly":
                Conversation cs = findCurrentCon();
                objectOutputStream.writeObject(cs);
                break;
            case "KnownPlaintext":
                break;
            case "ChoseCiphertext":
                break;
            case "ChosePlaintext":
                break;
        }
    }

    private Conversation findCurrentCon()
    {
        int pos = 0;
        for(int i = 0; i < ServerController.allCon.size(); i++)
        {
            if(ServerController.allCon.get(i).getClient1id() == conversation.getClient1id()
            && ServerController.allCon.get(i).getClient2id() == conversation.getClient2id()){
                pos= i;
                break;
            }
        }
        Conversation s = new Conversation();
        s.setClient1id(ServerController.allCon.get(pos).getClient1id());
        s.setClient2id(ServerController.allCon.get(pos).getClient2id());
        s.setTypeOfEncryption(ServerController.allCon.get(pos).getTypeOfEncryption());
        s.addMessageList(ServerController.allCon.get(pos).msgs);
        return s;
    }
}