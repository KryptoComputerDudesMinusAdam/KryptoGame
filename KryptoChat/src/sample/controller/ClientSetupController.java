package sample.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sample.model.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientSetupController {

    @FXML
    Button serverButton, receiverButton;
    @FXML
    TextField hostnameTextField, portTextField, clientNameTextField;
    @FXML
    ComboBox<String> encryptionComboBox;
    @FXML
    ListView<Message> contactsListView;
    private List<Message> contacts = new ArrayList<>();
    private ClientServerThread clientServerThread;

    public void handleServerButton(ActionEvent event){
        if(hostnameTextField.getText() != null && portTextField.getText() != null && clientNameTextField.getText() != null){
            String host = hostnameTextField.getText();
            int port = Integer.parseInt(portTextField.getText());
            clientServerThread = new ClientServerThread(this);
            clientServerThread.port = port;
            clientServerThread.host = host;
            clientServerThread.clientName = clientNameTextField.getText();
            clientServerThread.start();
        }
    }

    public void handleReceiverButton(ActionEvent event){
        try{
//            // display user interface
//            FXMLLoader loader = new FXMLLoader();
//            loader.setLocation(getClass().getResource("../view/ClientChatRoom.fxml"));
//            Parent root = loader.load();
//            ClientChatRoomController UI = loader.getController();
//            Controller.newWindow(root);
            clientServerThread.sendInvite(contactsListView.getSelectionModel().getSelectedItem().encryptedMessage);
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    void updateContacts(Message m){
        Platform.runLater(() -> {
            Controller.initializeListView(contacts, contactsListView);
            contacts.add(m);
            contactsListView.getItems().setAll(contacts);
            contactsListView.refresh();
        });
    }
}

class ClientServerThread extends Thread {
    String host;
    int port;
    String clientName;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private ClientSetupController clientSetupController;

    ClientServerThread(ClientSetupController clientSetupController){
        this.clientSetupController = clientSetupController;
    }

    public void run(){
        try {
            // Create a socket to connect to the server
            Socket socket = new Socket(host, port);

            // for writing and reading to sockets
            OutputStream outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            InputStream inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            // add your name into the server's contacts
            Message firstMessage = new Message(clientName);
            firstMessage.typeOfMessage = Message.clientName;
            objectOutputStream.writeObject(firstMessage);

            // listen in for incoming messages
            new Thread(()->{
                while(true) {
                    try {
                        Message m = (Message) objectInputStream.readObject();
                        switch (m.typeOfMessage) {
                            case Message.contacts:
                                // update contacts list if needed
                                if (clientSetupController.contactsListView.getItems().stream().noneMatch(co -> co.encryptedMessage.equals(m.encryptedMessage))) {
                                    Platform.runLater(() -> clientSetupController.updateContacts(m));
                                }
                                break;
                            case Message.conversationInvite:
                                // another user wants to connect
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, m.encryptedMessage + " invited you to chat.\nWould you like to connect?", ButtonType.YES, ButtonType.NO);
                                    alert.showAndWait();
                                    if(alert.getResult() == ButtonType.YES) {
                                        //do stuff
                                    }
                                });
                                break;
                            case Message.conversationAccept:
                                // another user accepted connection
                                break;
                            case Message.conversationDecline:
                                // another user declined connection
                                break;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    void sendInvite(String receiver){
        try {
            Message m = new Message(receiver);
            m.typeOfMessage = Message.conversationInvite;
            objectOutputStream.writeObject(m);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}