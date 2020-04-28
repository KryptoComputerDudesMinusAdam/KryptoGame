package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
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
        if(port.getText() != null && port.getText().length() > 0)
        {
            try {
                // initialize socket
                if(attack_socket == null) {
                    attack_socket = new Socket("0.0.0.0", Integer.parseInt(port.getText()));
                    objis = new ObjectInputStream(attack_socket.getInputStream());
                    objos = new ObjectOutputStream(attack_socket.getOutputStream());
                }

                // load proper attack window
                FXMLLoader loader = new FXMLLoader();
                String selected = comboBox.getValue().replaceAll(" ","").replaceAll("-","");
                System.out.println("Selected: "+selected);
                loader.setLocation(getClass().getResource("../view/"+selected+".fxml"));
                Parent root = loader.load();

                // initialize specific attacker controller
                AttackerSetupController UI = loader.getController();
                UI.objis = this.objis;
                UI.objos = this.objos;
                UI.init();

                // show specific attacker window and close current window
                Controller.newWindow(root);
                Stage stage = (Stage) connect.getScene().getWindow();
                stage.close();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR,e.getMessage()+"\nOops! Unable to connect.", ButtonType.OK);
                alert.showAndWait();
            }
        } else {
            // display potential errors
            String content;
            if(comboBox.getValue() == null)
                content = "Please select a mode of Attack!";
            else
                content = "Please enter the server port number!";
            Alert alert = new Alert(Alert.AlertType.ERROR,content, ButtonType.OK);
            alert.showAndWait();
            alert.showAndWait();
        }
    }

    // add data to the choice box
    private void addData() {
        comboBox.getItems().setAll("Ciphertext Only Attack",
                "Known-Plaintext Attack",
                "Chosen Plaintext Attack",
                "Chosen Ciphertext Attack");
        comboBox.getSelectionModel().selectFirst();
    }
}
