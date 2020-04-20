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


public class ClientChatRoomController {
    @FXML
    Label titleLabel;
    @FXML
    TextArea sendTextArea;
    @FXML
    Button sendButton, DecryptButton;
    @FXML
    TextField receiveTextField;
    @FXML
    ListView<Message> chatListView;
    List<Message> messages = new ArrayList<>();
    ClientThread client;
//    ObjectOutputStream objectOutputStream;
//    ObjectInputStream objectInputStream;

    void initializeThread(Socket socket, String clientName, String receiverId, ObjectOutputStream oos, ObjectInputStream ois){
       client = new ClientThread(socket, clientName, receiverId, oos, ois);
       client.clientChatRoomController = this;
       client.start();
    }

    void listenIn(){
        Controller.initializeListView(messages, chatListView);
    }

    public void handleSendButton(ActionEvent event){
        client.sendMessage(sendTextArea.getText());
    }

    public void handleDecryptButton(ActionEvent event){
        /*
            TODO:
                decrypt a selected message from listview and then update listview
         */
    }
}

class ClientThread extends Thread{
    Socket socket;
    String clientName;
    String receiverId;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;
    ClientChatRoomController clientChatRoomController;

    public ClientThread(Socket socket, String clientName, String receiverId, ObjectOutputStream oos, ObjectInputStream ois){
        this.socket = socket;
        this.clientName = clientName;
        this.receiverId = receiverId;
        this.objectOutputStream = oos;
        this.objectInputStream = ois;
    }

    public void run(){
        // constantly listen in for new messages
        new Thread(()->{
            try {
                System.out.println("Listening!!");
                while(true){
                    System.out.println("In while loop!");
                    Message m = (Message) objectInputStream.readObject();
                    System.out.println("Got a message! "+m.encryptedMessage);
                    m.encryptedMessage = "["+receiverId+"]: "+m.encryptedMessage;
                    Platform.runLater(() -> {
                        clientChatRoomController.messages.add(m);
                        clientChatRoomController.chatListView.getItems().setAll(clientChatRoomController.messages);
                        clientChatRoomController.chatListView.refresh();
                    });
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }).start();
    }

    void sendMessage(String str){
        try {
            System.out.println("Trying to send a message!");
            Message m = new Message(str);
            Platform.runLater(() -> {
                clientChatRoomController.sendTextArea.clear();
                m.encryptedMessage = "["+clientName+"]: "+m.encryptedMessage;
                clientChatRoomController.messages.add(m);
                clientChatRoomController.chatListView.getItems().setAll(clientChatRoomController.messages);
                clientChatRoomController.chatListView.refresh();
            });
            objectOutputStream.writeObject(m);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
