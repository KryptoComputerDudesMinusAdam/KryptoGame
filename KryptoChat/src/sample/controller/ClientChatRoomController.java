package sample.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import sample.model.Cipher;
import sample.model.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ClientChatRoomController {
    @FXML
    Label titleLabel;
    @FXML
    TextArea sendTextArea, receiveTextArea;
    @FXML
    Button sendButton, DecryptButton;
    @FXML
    ListView<Message> chatListView;
    List<Message> messages = new ArrayList<>();
    ClientThread client;

    void initializeThread(Socket socket, String clientName, String receiverId, ObjectOutputStream oos, ObjectInputStream ois){
       client = new ClientThread(socket, clientName, receiverId, oos, ois);
       client.clientChatRoomController = this;
       client.start();

       new Thread(()->{
           while(true){
               if(chatListView.getSelectionModel().getSelectedItem() != null){
                   DecryptButton.setDisable(false);
               }  else {
                   DecryptButton.setDisable(true);
               }
           }
       });
    }

    void listenIn(){
        Controller.initializeListView(messages, chatListView);
        titleLabel.setText(client.receiverId);
    }

    public void handleListViewClick(MouseEvent event){
        try{
            Message m = chatListView.getSelectionModel().getSelectedItem();
            receiveTextArea.setText(m.encryptedMessage);
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void handleSendButton(ActionEvent event){
        client.sendMessage(sendTextArea.getText());
    }

    public void handleDecryptButton(ActionEvent event){
        if(chatListView.getSelectionModel().getSelectedItem() != null
                && receiveTextArea.getText().equals(chatListView.getSelectionModel().getSelectedItem().encryptedMessage)
                    && chatListView.getSelectionModel().getSelectedItem().isEncrypted){
            System.out.println("Decrypting message: "+receiveTextArea.getText());
            Message m = chatListView.getSelectionModel().getSelectedItem();

            switch(client.typeOfCipher){
                case Message.cipherMonoAlphabetic:
                    m.encryptedMessage = Cipher.monoalphabeticDec(client.key, receiveTextArea.getText());
                    break;
                case Message.cipherVigenere:
                    m.encryptedMessage = Cipher.vigenereDec(client.key, receiveTextArea.getText());
                    break;
                case Message.cipherStream:
                    m.encryptedMessage = Cipher.monoalphabeticDec(client.key, receiveTextArea.getText());
                    break;
                default:
                    m.encryptedMessage = Cipher.monoalphabeticDec(client.key, receiveTextArea.getText());
                    break;
            }
            m.isEncrypted = false;
            chatListView.getItems().setAll(messages);
            chatListView.refresh();
        }
    }
}

class ClientThread extends Thread{
    Socket socket;
    String clientName;
    String receiverId;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;
    ClientChatRoomController clientChatRoomController;
    String key;
    String typeOfCipher;

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
                Message keyFromServer = (Message) objectInputStream.readObject();
                System.out.println("Received key: "+keyFromServer.encryptedMessage);
                key = keyFromServer.encryptedMessage;
                typeOfCipher = keyFromServer.typeOfCipher;

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
            String e;
            switch(typeOfCipher){
                case Message.cipherMonoAlphabetic:
                    e = Cipher.monoalphabeticEnc(key, str);
                    break;
                case Message.cipherVigenere:
                    e = Cipher.vigenereEnc(key, str);
                    break;
                case Message.cipherStream:
                    e = Cipher.monoalphabeticEnc(key, str);
                    break;
                default:
                    e = Cipher.monoalphabeticEnc(key, str);
                    break;
            }
            Message m = new Message(e);
            m.isEncrypted = true;
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
