package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import sample.model.Message;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class AttackerSetupController
{
    ObservableList<String> list = FXCollections.observableArrayList();
    @FXML
    private Button connect;
    @FXML
    private TextField port;
    @FXML
    private ChoiceBox<String> choiceBox;

    // Initialize choice box
    public void init()
    {
        addData();
    }


    /*
        TODO: try and connect to server
    */
    public void handleConnectButton(ActionEvent event)
    {
        if(port.getText() != null)
        {
            int pt = Integer.parseInt(port.getText());
            try
            {

            }catch (Exception e)
            {

            }
        }
    }

    //Add data to the choice box
    private void addData()
    {
        list.removeAll(list);
        list.addAll("Known-Plaintext Attack",
                "Ciphertext Only Attack",
                "Chosen Plaintext Attack",
                "Chosen Ciphertext Attack");
        choiceBox.getItems().addAll(list);
    }
}
