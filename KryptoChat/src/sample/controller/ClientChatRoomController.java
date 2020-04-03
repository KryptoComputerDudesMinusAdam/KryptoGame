package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class ClientChatRoomController {
    @FXML
    TextArea sendTextArea;
    @FXML
    Button sendButton, DecryptButton;
    @FXML
    TextField receiveTextField;
    @FXML
    ListView chatListView;

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
