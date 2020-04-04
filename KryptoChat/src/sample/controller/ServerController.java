package sample.controller;

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

    public void handleConnectButton(ActionEvent event) throws IOException{
        // add port number
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
        if(portTextField.getText() != null){
            int port = Integer.parseInt(portTextField.getText());
            try(ServerSocket serverSocket = new ServerSocket(port)){
                    // wait for connections
                    messages.add(new Message("Server is open for connections..."));
                    observableList.setAll(messages);
                    listView.setItems(observableList);
                    new Thread(() -> {
                        try {
                            Socket socket = serverSocket.accept();
                            messages.add(new Message("Connection received from port: "+port));
                            observableList.setAll(messages);
                            listView.setItems(observableList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void handleDisconnectButton(ActionEvent event){
        /*
            TODO:
                Add disconnect to server logic
         */
        messages.add(new Message("Server is open for connections..."));
        observableList.setAll(messages);
    }
}
