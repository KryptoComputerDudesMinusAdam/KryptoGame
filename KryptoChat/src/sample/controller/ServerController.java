package sample.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import sample.model.Message;
import sample.model.MessageList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void handleConnectButton(ActionEvent event){
        // initialize the list view
        initializeListView();

        if(portTextField.getText() != null){
            int port = Integer.parseInt(portTextField.getText());
            CustomServerThread customServerThread = new CustomServerThread(this);
            customServerThread.port = port;
            customServerThread.start();
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

class CustomServerThread extends Thread {
    int port;
    ServerController serverController;

    public CustomServerThread(ServerController serverController){
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
        List<Message> messages = new ArrayList<>();
        for(Map.Entry<Integer, String> entry : ServerController.contacts.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();
            messages.add(new Message(value));
        }
        MessageList ms = new MessageList(messages, Message.contacts);
        for(ServerClientThread sct : ServerController.clients){
            sct.objectOutputStream.writeObject(ms);
        }
    }
}

class ServerClientThread extends Thread {
    private Socket socket;
    private ServerController serverController;
    private CustomServerThread customServerThread;
    private OutputStream outputStream;
    public ObjectOutputStream objectOutputStream;
    private InputStream inputStream;
    private ObjectInputStream objectInputStream;

    public ServerClientThread(Socket socket, ServerController serverController, CustomServerThread customServerThread){
        this.socket = socket;
        this.serverController = serverController;
        this.customServerThread = customServerThread;
    }

    public void run(){
        String clientName;
        int clientPort;
        try {
            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            // read message from client: client name and add to contacts list
            Message m = (Message) objectInputStream.readObject();
            if(!ServerController.contacts.containsValue(m.encryptedMessage)){
                clientName = m.encryptedMessage;
                clientPort = socket.getPort();
                ServerController.contacts.put(clientPort, clientName);
                serverController.displayNewMessage(new Message("Added "+ServerController.contacts.get(clientPort)));
                customServerThread.broadcastContactsList();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}