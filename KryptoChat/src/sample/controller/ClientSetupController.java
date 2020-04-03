package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ClientSetupController {

    @FXML
    Button serverButton, receiverButton;
    @FXML
    TextField hostnameTextField, serverTextField, clientNameTextField;
    @FXML
    ComboBox<String> encryptionComboBox;
    @FXML
    ListView<String> contactsListView;

    public void handleServerButton(ActionEvent event){
        /*
            TODO:
                try and connect to server
         */
        if(hostnameTextField.getText() != null &&
                serverTextField.getText() != null &&
                clientNameTextField.getText() != null){
            
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
}
