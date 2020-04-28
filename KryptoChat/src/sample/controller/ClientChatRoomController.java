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
import sample.model.RSA;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
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
    private ClientThread client;

    void initializeThread(Socket socket, String clientName, String receiverId, ObjectOutputStream oos, ObjectInputStream ois){
       client = new ClientThread(socket, clientName, receiverId, oos, ois);
       client.clientChatRoomController = this;
       client.start();

       // UI refresher thread
       new Thread(()-> {
           while(true){
               if(chatListView.getSelectionModel().getSelectedItem() != null){
                   DecryptButton.setDisable(false);
               }  else {
                   DecryptButton.setDisable(true);
               }
           }
       }).start();
    }

    void init() {
        Controller.initializeListView(messages, chatListView);
        titleLabel.setText((client.receiverId).replaceAll("[^a-zA-Z]",""));
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

    public void handleDecryptButton(ActionEvent event) throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
        if(chatListView.getSelectionModel().getSelectedItem() != null
                    && chatListView.getSelectionModel().getSelectedItem().isEncrypted) {
            Message m = chatListView.getSelectionModel().getSelectedItem();
            String[] headAndTail = getHeadAndTail(receiveTextArea.getText());
            switch(client.typeOfCipher){
                case Message.cipherMonoAlphabetic:
                    m.message = headAndTail[0] + Cipher.monoalphabeticDec(client.publicKey, headAndTail[1]).toLowerCase();
                    break;
                case Message.cipherVigenere:
                    m.message = headAndTail[0] + Cipher.vigenereDec(client.publicKey, headAndTail[1]).toLowerCase();
                    break;
                case Message.cipherStream:
                    m.message = headAndTail[0] + Cipher.streamDec(client.publicKey, headAndTail[1]).toLowerCase();
                    break;
                case Message.cipherRSA:
                    m.message = headAndTail[0] +  RSA.dec(headAndTail[1], client.privateKey);
                    break;
            }
            m.isEncrypted = false;
            chatListView.getItems().setAll(messages);
            chatListView.refresh();
        }
    }

    private String[] getHeadAndTail(String str){
        char[] testChar = str.toCharArray();
        int i = 0;
        while(testChar[i] != ':'){
            i++;
        }
        return new String[]{str.substring(0, i+2), str.substring(i+2)};
    }
}

class ClientThread extends Thread{
    private Socket socket;
    private String clientName;
    String receiverId;
    ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    ClientChatRoomController clientChatRoomController;
    String publicKey;
    String privateKey;
    String typeOfCipher;

    ClientThread(Socket socket, String clientName, String receiverId, ObjectOutputStream oos, ObjectInputStream ois){
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
                Message publicKeyMessage = (Message) objectInputStream.readObject();
                System.out.println("Received key: "+publicKeyMessage.message);
                publicKey = publicKeyMessage.message;
                typeOfCipher = publicKeyMessage.typeOfCipher;

                // check if you need to expect a private key too for RSA
                if(typeOfCipher.equals(Message.cipherRSA)){
                    Message privateKeyMessage = (Message) objectInputStream.readObject();
                    System.out.println("Received private key: "+privateKeyMessage.message);
                    privateKey = privateKeyMessage.message;
                }

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
                        m.message = "["+receiverId.replaceAll("[^a-zA-Z]","")+"]: "+m.message;
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
                    e = Cipher.monoalphabeticEnc(publicKey, str);
                    break;
                case Message.cipherVigenere:
                    e = Cipher.vigenereEnc(publicKey, str);
                    break;
                case Message.cipherStream:
                    e = Cipher.streamEnc(publicKey, str);
                    break;
                case Message.cipherRSA:
                    e = Base64.getEncoder().encodeToString(RSA.enc(str, publicKey));
                    break;
                default:
                    e = Cipher.monoalphabeticEnc(publicKey, str);
                    break;
            }
            Message m = new Message(e);
            m.typeOfMessage = Message.simpleMessage;
            m.isEncrypted = true;
            Platform.runLater(() -> {
                clientChatRoomController.sendTextArea.clear();
                m.message = "["+clientName.replaceAll("[^a-zA-Z]","")+"]: "+str;
                m.isEncrypted = false;
                clientChatRoomController.messages.add(m);
                clientChatRoomController.chatListView.getItems().setAll(clientChatRoomController.messages);
                clientChatRoomController.chatListView.refresh();
            });
            objectOutputStream.writeObject(m);
        } catch (IOException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }
}
