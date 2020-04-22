package sample.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sample.model.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

        encryptionComboBox.getItems().setAll(Message.cipherMonoAlphabetic, Message.cipherVigenere, Message.cipherStream);
        encryptionComboBox.getSelectionModel().selectFirst();
    }

    public void handleReceiverButton(ActionEvent event){
        try{
            String receiver = contactsListView.getSelectionModel().getSelectedItem().encryptedMessage;
            String typeOfMessage = Message.conversationInvite;
            String typeOfCipher = encryptionComboBox.getSelectionModel().getSelectedItem();
            clientServerThread.sendMessage(receiver, typeOfMessage, typeOfCipher);
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    void updateContacts(Message m){
        Platform.runLater(() -> {
            Controller.initializeListView(contacts, contactsListView);
            if(!m.encryptedMessage.equalsIgnoreCase(clientServerThread.clientId)){
                contacts.add(m);
                contactsListView.getItems().setAll(contacts);
                contactsListView.refresh();
            }
        });
    }

    void displayAlert(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }

    void displayChatRoom(ClientServerThread cst){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/ClientChatRoom.fxml"));
            Parent root;
            root = loader.load();
            ClientChatRoomController UI = loader.getController();
            UI.initializeThread(cst.socket, cst.clientId, cst.receivingClient, cst.objectOutputStream, cst.objectInputStream);
            UI.listenIn();
            Controller.newWindow(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientServerThread extends Thread {
    String host;
    int port;
    String clientName;
    String clientId;
    Socket socket;
    String receivingClient;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;
    private ClientSetupController clientSetupController;

    ClientServerThread(ClientSetupController clientSetupController){
        this.clientSetupController = clientSetupController;
    }

    public void run(){
        try {
            // Create a socket to connect to the server
            socket = new Socket(host, port);

            Platform.runLater(()->{
                clientSetupController.serverButton.setDisable(true);
                clientSetupController.receiverButton.setDisable(false);
            });

            // for writing and reading to sockets
            OutputStream outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            InputStream inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            // add your name into the server's contacts
            Message firstMessage = new Message(clientName);
            clientName+="#"+socket.getPort();
            firstMessage.typeOfMessage = Message.clientName;
            objectOutputStream.writeObject(firstMessage);

            // listen in for incoming messages
            new Thread(()->{
                boolean foundConversation = false;
                while(!foundConversation) {
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
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, m.encryptedMessage + " invited you to chat with encryption type: "+m.typeOfCipher+"\nWould you like to connect?", ButtonType.YES, ButtonType.NO);
                                    alert.showAndWait();
                                    if(alert.getResult() == ButtonType.YES) {
                                        sendMessage(m.encryptedMessage, Message.conversationAccept, m.typeOfCipher);
                                    } else if(alert.getResult() == ButtonType.NO){
                                        sendMessage(m.encryptedMessage, Message.conversationDecline, m.typeOfCipher);
                                    }
                                });
                                receivingClient = m.encryptedMessage;
                                foundConversation = true;
                                break;
                            case Message.conversationAccept:
                                // another user accepted connection
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, m.encryptedMessage + " accepted the invite!\nYou two will be talking with encryption type: "+m.typeOfCipher, ButtonType.OK);
                                    alert.showAndWait();
                                });
                                receivingClient = m.encryptedMessage;
                                foundConversation = true;
                                break;
                            case Message.conversationDecline:
                                // another user declined connection
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, m.encryptedMessage + " declined the invite.", ButtonType.OK);
                                    alert.showAndWait();
                                });
                                break;
                            case Message.conversationUniqueID:
                                // another user declined connection
                                this.clientId = m.encryptedMessage;
                                break;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("No longer reading messages here");
                Platform.runLater(() -> {
                    clientSetupController.displayChatRoom(this);
                    Stage stage = (Stage) clientSetupController.serverButton.getScene().getWindow();
                    stage.close();
                });

            }).start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            Platform.runLater(() -> {
                clientSetupController.displayAlert("Failure: Port is either busy or does not exist.");
            });
        }
    }

    void sendMessage(String receiver, String typeOfMessage, String typeOfCipher){
        try {
            System.out.println("MESSAGE INPUT: "+typeOfCipher);
            Message m = new Message(receiver, typeOfCipher);
            m.typeOfMessage = typeOfMessage;
            System.out.println("MESSAGE TEST: "+m.typeOfCipher);
            objectOutputStream.writeObject(m);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}