package sample.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sample.model.Message;
import sample.model.MessageList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerController {

    @FXML
    Button connectButton, disconnectButton;
    @FXML
    TextField portTextField;
    @FXML
    ListView<Message> listView;
    private ObservableList<Message> observableList;
    private List<Message> messages = new ArrayList<>();
    public static List<ServerClientThread> clients = new ArrayList<>();
    public static HashMap<Integer, String> contacts = new HashMap<>();
    public static HashMap<String, String> converstations = new HashMap<>();

    public void handleConnectButton(ActionEvent event){
        // initialize the list view
        initializeListView();

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

    private void initializeListView(){
        observableList = FXCollections.observableArrayList(messages);
        listView.setItems(observableList);
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null || message.encryptedMessage == null) {
                    setText(null);
                } else {
                    setText(message.encryptedMessage);
                }
            }
        });
    }

    public void displayNewMessage(Message m){
        Platform.runLater(() -> {
            messages.add(m);
            observableList.setAll(messages);
            listView.setItems(observableList);
        });
    }
}

class ServerThread extends Thread {
    int port;
    ServerController serverController;

    public ServerThread(ServerController serverController){
        this.serverController = serverController;
    }

    public void run(){
        try {
            // create a server socket
            ServerSocket serverSocket = new ServerSocket(port);

            // continuously look for connections
            while (true) {
                serverController.displayNewMessage(new Message("Server is open for connection..."));
                Socket socket = serverSocket.accept();
                serverController.displayNewMessage(new Message("Received connection from port " + socket.getPort()));

                // start and add thread to list of threads
                ServerClientThread client = new ServerClientThread(socket, serverController, this);
                client.start();
                serverController.clients.add(client);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void broadcastContactsList() throws IOException {
        // send message to client: contacts list
        for(ServerClientThread sct : ServerController.clients){
            for(Map.Entry<Integer, String> entry : ServerController.contacts.entrySet()){
                Message contact = new Message(entry.getValue());
                contact.typeOfMessage = Message.contacts;
                sct.objectOutputStream.writeObject(contact);
            }
        }
    }
}

class ServerClientThread extends Thread {
    private Socket socket;
    private ServerController serverController;
    private ServerThread serverThread;
    private OutputStream outputStream;
    public ObjectOutputStream objectOutputStream;
    private InputStream inputStream;
    private ObjectInputStream objectInputStream;
    Queue<Message> invites = new LinkedList<>();
    String clientName = "";
    int clientPort;

    public ServerClientThread(Socket socket, ServerController serverController, ServerThread serverThread){
        this.socket = socket;
        this.serverController = serverController;
        this.serverThread = serverThread;
        this.clientPort = socket.getPort();
    }

    public void run(){

        try {
            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            // read message from client: client name and add to contacts list
            Message m = (Message) objectInputStream.readObject();
            if(!ServerController.contacts.containsValue(m.encryptedMessage)){
                clientName = m.encryptedMessage;
                ServerController.contacts.put(clientPort, clientName);
                serverController.displayNewMessage(new Message("Added "+ServerController.contacts.get(clientPort)));
                serverThread.broadcastContactsList();
            }

            // continuously check for invites coming from client
            new Thread(()->{
                try {
                    while(true){
                        Message message = (Message) objectInputStream.readObject();
                        for(ServerClientThread client : ServerController.clients){
                            if(client.clientName.equals(message.encryptedMessage)){
                                Message inviteMessage = new Message(clientName);
                                inviteMessage.typeOfMessage = Message.conversationInvite;
                                client.objectOutputStream.writeObject(inviteMessage);
                                break;
                            }
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