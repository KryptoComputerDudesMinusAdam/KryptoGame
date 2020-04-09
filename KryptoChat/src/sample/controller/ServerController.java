package sample.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    public static List<ServerClientThread> clients = new ArrayList<>();
    public static HashMap<Integer, String> contacts = new HashMap<>();

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

class ServerThread extends Thread {
    int port;
    private ServerController serverController;

    public ServerThread(ServerController serverController){
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
                ServerController.clients.add(sct);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void broadcastContactsList() {
        // send message to client: contacts list
        try {
            for (ServerClientThread sct : ServerController.clients) {
                for (Map.Entry<Integer, String> entry : ServerController.contacts.entrySet()) {
                    Message contact = new Message(entry.getValue());
                    contact.typeOfMessage = Message.contacts;
                    sct.objectOutputStream.writeObject(contact);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// represents a single connection to a user
class ServerClientThread extends Thread {
    private ServerController serverController;
    private ServerThread serverThread;
    private Socket socket;

    private String clientName = "";
    private int clientPort;
    ObjectOutputStream objectOutputStream;
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
            m.encryptedMessage+="#"+socket.getPort();
            if(!ServerController.contacts.containsValue(m.encryptedMessage)){
                clientName = m.encryptedMessage;
                ServerController.contacts.put(clientPort, clientName);
                serverController.displayNewMessage(new Message("Added "+ServerController.contacts.get(clientPort)));
                serverThread.broadcastContactsList();
            }

            // continuously check for messages coming from client
            new Thread(()->{
                try {
                    while(true){
                        Message message = (Message) objectInputStream.readObject();
                        switch(message.typeOfMessage){
                            case Message.conversationInvite:
                                // client wants to send an invitation
                                for(ServerClientThread client : ServerController.clients){
                                    if(client.clientName.equals(message.encryptedMessage)){
                                        Message inviteMessage = new Message(clientName);
                                        inviteMessage.typeOfMessage = Message.conversationInvite;
                                        client.objectOutputStream.writeObject(inviteMessage);
                                        break;
                                    }
                                }
                                break;
                            case Message.conversationDecline:
                                // client wants to decline an invitation
                                break;
                            case Message.conversationAccept:
                                // client wants to accept and invitation
                                break;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}