package sample.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import sample.model.Message;
import sample.model.MessageList;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

    public void handleServerButton(ActionEvent event){
        if(hostnameTextField.getText() != null &&
                serverTextField.getText() != null &&
                clientNameTextField.getText() != null){
            String host = hostnameTextField.getText();
            int port = Integer.parseInt(serverTextField.getText());
            new Thread(() -> {
                try(Socket socket = new Socket(host, port)) {
                    // for sending messages
                    OutputStream outputStream = socket.getOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

                    // for receiving messages
                    InputStream inputStream = socket.getInputStream();
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                    // add your name into the server's contacts list
                    objectOutputStream.writeObject(new Message(clientNameTextField.getText()));

                    // wait for contacts list to render
                    initializeListView();
                    new Thread(() ->{ // continuously update list view with contacts list
                        try {
                            MessageList ms = (MessageList) objectInputStream.readObject();
                            if(ms.typeOfMessage != null && ms.typeOfMessage.equals(Message.contacts)){
                                displayContacts(ms);
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }).start();
            }
    }

    public void handleReceiverButton(ActionEvent event){
        try{
            // display user interface
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/ClientChatRoom.fxml"));
            Parent root = loader.load();
            ClientChatRoomController UI = loader.getController();
            Controller.newWindow(root);
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

    public void displayContacts(MessageList ms){
        Platform.runLater(() -> {
            contacts.clear();
            contacts.addAll(ms.messages);
            observableList.setAll(contacts);
            contactsListView.setItems(observableList);
        });
    }
}