package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


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
    ListView chatListView;
    Socket socket;
    String clientName;
    String receiverId;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    public void handleSendButton(ActionEvent event){
        /*
            TODO:
                send a message out to server
         */
    }

    public void handleDecryptButton(ActionEvent event){
        /*
            TODO:
                decrypt a selected message from listview and then update listview
         */
    }
}
