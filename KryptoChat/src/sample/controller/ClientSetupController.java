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
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ClientSetupController {

    @FXML
    Button serverButton, receiverButton;
    @FXML
    TextField hostnameTextField, serverTextField, clientNameTextField;
    @FXML
    ComboBox<String> encryptionComboBox;
    @FXML
    ListView<Message> contactsListView;
    private ObservableList<Message> observableList;
    private List<Message> contacts = new ArrayList<>();
    private ClientServerThread clientServerThread;

    public void handleServerButton(ActionEvent event){
        if(hostnameTextField.getText() != null && serverTextField.getText() != null && clientNameTextField.getText() != null){
            String host = hostnameTextField.getText();
            int port = Integer.parseInt(serverTextField.getText());
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

    private void initializeListView(){
        observableList = FXCollections.observableArrayList(contacts);
        contactsListView.setItems(observableList);
        contactsListView.setCellFactory(param -> new ListCell<>() {
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

    void updateContacts(Message m){
        Platform.runLater(() -> {
            initializeListView();
            contacts.add(m);
            observableList.setAll(contacts);
            contactsListView.setItems(observableList);
        });
    }
}

class ClientServerThread extends Thread {
    String host;
    int port;
    String clientName;
    private Socket socket;
    private OutputStream outputStream;
    private ObjectOutputStream objectOutputStream;
    private InputStream inputStream;
    private ObjectInputStream objectInputStream;
    private ClientSetupController clientSetupController;
    Queue<MessageList> contactsQueue = new LinkedList<>();
    public Queue<Message> messageQueue = new LinkedList<>();

    public ClientServerThread(ClientSetupController clientSetupController){
        this.clientSetupController = clientSetupController;
    }

    public void run(){
        try {
            // Create a socket to connect to the server
            socket = new Socket(host, port);

            // for sending messages
            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);

            // for receiving messages
            inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            // add your name into the server's contacts list
            objectOutputStream.writeObject(new Message(clientName));

            // listen in for updated contacts list
            new Thread(()->{
                while(true) {
                    try {
                        Message m = (Message) objectInputStream.readObject();
                        if(m.typeOfMessage.equals(Message.contacts)){
                            if(!clientSetupController.contactsListView.getItems().stream().anyMatch(co -> co.encryptedMessage.equals(m.encryptedMessage))){
                                clientSetupController.updateContacts(m);
                            }
                        } else if(m.typeOfMessage.equals(Message.conversationInvite)){
                            // display confirmation pop up that somebody wants to connect with you
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, m.encryptedMessage + " invited you to chat.\nWould you like to connect?", ButtonType.YES, ButtonType.NO);
                                alert.showAndWait();

                                if (alert.getResult() == ButtonType.YES) {
                                    //do stuff
                                }
                            });

                        } else if (m.typeOfMessage.equals(Message.conversationAccept)) {

                        } else if (m.typeOfMessage.equals(Message.conversationDecline)){

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

    public void sendInvite(String receiver){
        try {
            System.out.println("Sent invite");
            Message m = new Message(receiver);
            m.typeOfMessage = Message.conversationInvite;
            objectOutputStream.writeObject(m);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}