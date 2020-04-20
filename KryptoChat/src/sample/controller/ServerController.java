package sample.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import sample.model.Conversation;
import sample.model.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerController {

    @FXML Button connectButton, disconnectButton;
    @FXML TextField portTextField;
    @FXML ListView<Message> listView;
    private List<Message> messages = new ArrayList<>();

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

    private int clientPort;
    String clientId;
    ObjectOutputStream objectOutputStream;
    private boolean foundConversation = false;
    private ObjectInputStream objectInputStream;

    ServerClientThread(Socket socket, ServerController serverController, ServerThread serverThread){
        this.socket = socket;
        this.serverController = serverController;
        this.serverThread = serverThread;
        this.clientPort = socket.getPort();
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

            // Attacker
            if(clientName.startsWith("Attacker")){
                handelAttacker(objectOutputStream, objectInputStream, clientName);
            }else{
                serverController.displayNewMessage(new Message("Added "+clientId));
                serverThread.broadcastContactsList();
            // continuously check for messages coming from client
            new Thread(()->{
                try {
                    // try to get two clients to connect to each other
                    while(true){
                        Message message = (Message) objectInputStream.readObject();
                        // client still needs to connect to another client
                        if(!foundConversation){
                            for(ServerClientThread client : ServerThread.clients){
                                if(client.clientId.equals(message.encryptedMessage)){
                                    Message messageToOtherClient = new Message(clientId);
                                    messageToOtherClient.typeOfMessage = message.typeOfMessage;
                                    client.objectOutputStream.writeObject(messageToOtherClient);

                                    // connect the two clients together
                                    if(messageToOtherClient.typeOfMessage.equals(Message.conversationAccept)){
                                        String client2Id = message.encryptedMessage;
                                        ServerThread.connections.put(clientId, client2Id);
                                        ServerThread.connections.put(client2Id, clientId);
                                        serverController.displayNewMessage(new Message("New conversation: " + clientId + " and " + ServerThread.connections.get(clientId)));
                                        foundConversation = true;
                                        client.foundConversation = true;
                                    }
                                    break;
                                }
                            }
                        } else {
                            for(ServerClientThread client : ServerThread.clients){
                                if(client.clientId.equals(ServerThread.connections.get(clientId))){
                                    System.out.println("Writing message: "+message.encryptedMessage+"\n\tFrom: "+clientId + " to " + ServerThread.connections.get(clientId));
                                    client.objectOutputStream.writeObject(message);
                                    break;
                                }
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

    private void handelAttacker(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, String clientName) throws IOException {
        switch (clientName)
        {
            case "CiphertextOnly":
                break;
            case "KnownPlaintext":
                break;
            case "ChoseCiphertext":
                break;
            case "ChosePlaintext":
                break;
        }
        Conversation cs = new Conversation();
        cs.add(new Message("Message 1"));
        cs.add(new Message("Message 2"));
        cs.add(new Message("Message 3"));
        cs.add(new Message("Message 4"));
        objectOutputStream.writeObject(cs);
    }
}