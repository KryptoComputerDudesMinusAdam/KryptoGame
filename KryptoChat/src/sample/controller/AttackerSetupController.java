package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sample.model.Message;

import java.io.*;
import java.net.Socket;

public class AttackerSetupController implements Serializable
{
    static Socket attack_socket;
    ObjectInputStream objis;
    ObjectOutputStream objos;
    volatile boolean socketClosed = false;

    @FXML
    private Button connect;
    @FXML
    private TextField port;
    @FXML
    ComboBox<String> comboBox;

    // Initialize choice box
    public void init() throws IOException {
        addData();
    }

    public void handleConnectButton(ActionEvent event)
    {
        if(port.getText() != null)
        {
            int pt = Integer.parseInt(port.getText());
            try {
                if(attack_socket == null) {
                    attack_socket = new Socket("0.0.0.0", pt);
                    objis = new ObjectInputStream(attack_socket.getInputStream());
                    objos = new ObjectOutputStream(attack_socket.getOutputStream());
                }
                FXMLLoader loader = new FXMLLoader();
                String selected = comboBox.getValue().replaceAll(" ","").replaceAll("-","");
                System.out.println("Selected: "+selected);
                loader.setLocation(getClass().getResource("../view/"+selected+".fxml"));
                Parent root = loader.load();
                AttackerSetupController UI = loader.getController();
                UI.objis = this.objis;
                UI.objos = this.objos;
                UI.init();
                Controller.newWindow(root);
                Stage stage = (Stage) connect.getScene().getWindow();
                stage.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
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
            if(comboBox.getValue() == null)
                alert.setContentText("Please select a mode of Attack!");
            else
                alert.setContentText("Please enter the server port number!");
            alert.showAndWait();
        }
    }

    // add data to the choice box
    private void addData()
    {
        comboBox.getItems().setAll("Known-Plaintext Attack",
                "Ciphertext Only Attack",
                "Chosen Plaintext Attack",
                "Chosen Ciphertext Attack");
        comboBox.getSelectionModel().selectFirst();
    }
}
