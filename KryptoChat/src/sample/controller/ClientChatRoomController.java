package sample.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
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
    Button sendButton, DecryptButton, leaveButton;
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
       }).start();
    }

    void listenIn(){
        Controller.initializeListView(messages, chatListView);
        titleLabel.setText(client.receiverId);
    }

    public void handleListViewClick(MouseEvent event){
        try{
            Message m = chatListView.getSelectionModel().getSelectedItem();
            receiveTextArea.setText(m.message);
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void handleLeaveButton(ActionEvent event){
        try {
            Message terminate = new Message("terminate");
            terminate.typeOfMessage = Message.terminate;
            client.objectOutputStream.writeObject(terminate);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/ClientSetup.fxml"));
            Parent root;
            root = loader.load();
            ClientSetupController UI = loader.getController();
            Controller.newWindow(root);

            Stage stage = (Stage) this.leaveButton.getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void handleSendButton(ActionEvent event){
        client.sendMessage(sendTextArea.getText());
    }

    public void handleDecryptButton(ActionEvent event){
        if(chatListView.getSelectionModel().getSelectedItem() != null
                    && chatListView.getSelectionModel().getSelectedItem().isEncrypted) {
            System.out.println("Decrypting message: "+receiveTextArea.getText());
            Message m = chatListView.getSelectionModel().getSelectedItem();
            String[] headAndTail = getHeadAndTail(receiveTextArea.getText());
            System.out.println("HEAD: "+headAndTail[0]);
            System.out.println("TAIL: "+headAndTail[1]);
            switch(client.typeOfCipher){
                case Message.cipherMonoAlphabetic:
                    m.message = headAndTail[0] + Cipher.monoalphabeticDec(client.key, headAndTail[1]).toLowerCase();
                    break;
                case Message.cipherVigenere:
                    m.message = headAndTail[0] + Cipher.vigenereDec(client.key, headAndTail[1]).toLowerCase();
                    break;
                case Message.cipherStream:
                    m.message = headAndTail[0] + Cipher.streamDec(client.key, headAndTail[1]).toLowerCase();
                    break;
                default:
                    m.message = headAndTail[0] + Cipher.monoalphabeticDec(client.key, headAndTail[1]).toLowerCase();
                    break;
            }
            m.isEncrypted = false;
            System.out.println("Decrypted message: "+m.message);
            chatListView.getItems().setAll(messages);
            chatListView.refresh();
        }
    }

    public String[] getHeadAndTail(String str){
        char[] testChar = str.toCharArray();
        int i = 0;
        while(testChar[i] != ':'){
            i++;
        }

        return new String[]{str.substring(0, i+2), str.substring(i+2)};
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
                System.out.println("Received key: "+keyFromServer.message);
                key = keyFromServer.message;
                typeOfCipher = keyFromServer.typeOfCipher;

                while(true){
                    System.out.println("In while loop!");
                    Message m = (Message) objectInputStream.readObject();
                    if(m.typeOfMessage.equals(Message.terminate)) {
                        Platform.runLater(() -> {
                            try {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION, receiverId + " has left the chat session.");
                                alert.showAndWait();

                                FXMLLoader loader = new FXMLLoader();
                                loader.setLocation(getClass().getResource("../view/ClientSetup.fxml"));
                                Parent root;
                                root = loader.load();
                                ClientSetupController UI = loader.getController();
                                Controller.newWindow(root);

                                Stage stage = (Stage) clientChatRoomController.leaveButton.getScene().getWindow();
                                stage.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else{
                        System.out.println("Got a message! "+m.message);
                        m.message = "["+receiverId+"]: "+m.message;
                        Platform.runLater(() -> {
                            clientChatRoomController.messages.add(m);
                            clientChatRoomController.chatListView.getItems().setAll(clientChatRoomController.messages);
                            clientChatRoomController.chatListView.refresh();
                        });
                    }
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
                    e = Cipher.streamEnc(key, str);
                    break;
                default:
                    e = Cipher.monoalphabeticEnc(key, str);
                    break;
            }
            Message m = new Message(e);
            m.typeOfMessage = Message.simpleMessage;
            m.isEncrypted = true;
            Platform.runLater(() -> {
                clientChatRoomController.sendTextArea.clear();
                m.message = "["+clientName+"]: "+m.message;
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
