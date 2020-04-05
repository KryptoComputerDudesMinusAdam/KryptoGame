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
import java.util.concurrent.TimeUnit;

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
        initializeListView();

        // add port number
        if(portTextField.getText() != null){
            int port = Integer.parseInt(portTextField.getText());
            // wait for connections
            new Thread(() -> {
                try(ServerSocket serverSocket = new ServerSocket(port)) {
                    while(true) {
                        // look for connection
                        displayNewMessage(new Message("Server is open for connection..."));
                        Socket socket = serverSocket.accept();

                        // add new client and run a thread
                        displayNewMessage(new Message("Received connection from port " + socket.getPort()));
                        ServerClientThread client = new ServerClientThread(socket, this);
                        client.start();
                        clients.add(client);
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }).start();
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

class ServerClientThread extends Thread {
    private Socket socket;
    private ServerController serverController;

    public ServerClientThread(Socket socket, ServerController serverController){
        this.socket = socket;
        this.serverController = serverController;
    }

    public void start(){
            String clientName;
            int clientPort;
            try {
                new Thread(()->{
                    while(true){
                        try {
                            TimeUnit.SECONDS.sleep(2);
                            System.out.println("Status: "+socket.isClosed());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                // for sending messages
                OutputStream outputStream = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

                // for receiving messages
                InputStream inputStream = socket.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                // first message from client must contain their name
                Message m = (Message) objectInputStream.readObject();
                while(true){
                    if(!ServerController.contacts.containsValue(m.encryptedMessage)){
                        clientName = m.encryptedMessage;
                        clientPort = socket.getPort();
                        ServerController.contacts.put(clientPort, clientName);
                        break;
                    }
                }
                serverController.displayNewMessage(new Message("Added "+ServerController.contacts.get(clientPort)));


                while(true){
                    System.out.println("in while");
                    // send contacts list to client
                    List<Message> messages = new ArrayList<>();
                    for(Map.Entry<Integer, String> entry : ServerController.contacts.entrySet()) {
                        Integer key = entry.getKey();
                        String value = entry.getValue();
                        messages.add(new Message(value));
                    }
                    MessageList ms = new MessageList(messages, Message.contacts);
                    objectOutputStream.writeObject(ms);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
    }
}