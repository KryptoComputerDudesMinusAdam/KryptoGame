package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;


public class AttackerSetupController {
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

    public void handleConnectButton(ActionEvent event)
    {
        /*
            TODO: try and connect to server
         */
        try {
            if(port.getText() != null)
            {
                int i = Integer.parseInt(port.getText().toString());
            }

        }catch (NumberFormatException e)
        {

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
