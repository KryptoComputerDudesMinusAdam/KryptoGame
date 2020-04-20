package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class AttackerSetupController implements Serializable
{
    protected static Socket attack_socket;
    protected static ObjectInputStream objis;
    protected static ObjectOutputStream objos;

    private String selected;
    ObservableList<String> list = FXCollections.observableArrayList();
    @FXML
    private Button connect;
    @FXML
    private TextField port;
    @FXML
    private ChoiceBox<String> choiceBox;

    // Initialize choice box
    public void init() throws IOException {
        addData();
    }


    /*
        TODO: try and connect to server
    */
    public void handleConnectButton(ActionEvent event)
    {
        if(port.getText().length() != 0 && choiceBox.getValue() != null)
        {
            setSelection();
            int pt = Integer.parseInt(port.getText());
            try {
                if(attack_socket == null){
                    attack_socket = new Socket("0.0.0.0", pt);
                    objis = new ObjectInputStream((InputStream) attack_socket.getInputStream());
                    objos = new ObjectOutputStream((OutputStream) attack_socket.getOutputStream());
                }
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("../view/"+selected+".fxml"));
                Parent root = loader.load();
                AttackerSetupController UI = loader.getController();
                Controller.newWindow(root);
                UI.init();
                Stage stage = (Stage) connect.getScene().getWindow();
                stage.close();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info Box");
                alert.setHeaderText("Ooops!");
                alert.setContentText("Unable to connect,\ninput correct credentials");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("Ooops!");
            if(choiceBox.getValue() == null)
                alert.setContentText("Please select a mode of Attack!");
            else
                alert.setContentText("Please enter the server port number!");
            alert.showAndWait();
        }
    }

    //Add data to the choice box
    private void addData()
    {
        list.addAll("Known-Plaintext Attack",
                "Ciphertext Only Attack",
                "Chosen Plaintext Attack",
                "Chosen Ciphertext Attack");
        choiceBox.getItems().addAll(list);
    }

    public void setSelection()
    {
        switch (choiceBox.getValue())
        {
            case "Known-Plaintext Attack":
                this.selected = "KnownPlaintextAttack";
                break;
            case "Ciphertext Only Attack":
                this.selected ="CiphertextOnlyAttack";
                break;
            case "Chosen Plaintext Attack":
                this.selected ="ChosenPlaintextAttack";
                break;
            case "Chosen Ciphertext Attack":
                this.selected ="ChosenCiphertextAttack";
                break;
        }
    }
}
