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


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerController {

    @FXML
    Button connectButton, disconnectButton;
    @FXML
    TextField portTextField;
    @FXML
    ListView<Message> listView;
    private ObservableList<Message> observableList;
    private List<Message> messages = new ArrayList<>();

    public ServerController(){

    }

    public void handleConnectButton(ActionEvent event){
        initializeListView();

        // add port number
        if(portTextField.getText() != null){
            int port = Integer.parseInt(portTextField.getText());
            //new ServerSocketThread(port, this).start();


                    // wait for connections
                    new Thread(() -> {
                        try(ServerSocket serverSocket = new ServerSocket(port)) {
                            while(true) {
                                displayNewMessage(new Message("Server is open for connection..."));
                                Socket socket = serverSocket.accept();
                                displayNewMessage(new Message("Received connection from port " + socket.getPort()));
                            }
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }).start();


                    // create a client thread and add client thread into list of clients
//                ServerThread client = new ServerThread(socket);
//                client.start();
//                clients.add(client);
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

class ServerSocketThread extends Thread{
    private int port;
    private ServerController serverController;

    public ServerSocketThread(int port, ServerController serverController){
        this.port = port;
        this.serverController = serverController;
    }

    public void run(){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while(true){
                // wait for connections
                serverController.displayNewMessage(new Message("Server is open for connection..."));
                Socket socket = serverSocket.accept();
                serverController.displayNewMessage(new Message("Recevied connection from port "+socket.getPort()));


                // create a client thread and add client thread into list of clients
//                ServerThread client = new ServerThread(socket);
//                client.start();
//                clients.add(client);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
