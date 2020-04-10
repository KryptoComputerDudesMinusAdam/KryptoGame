package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class KnownPlaintextAttack extends AttackerSetupController
{
    @FXML
    Button disconnect, query_server, run_analysis, copy_message;

    @FXML
    ListView<String> ciphertext, plaintext, response;

    public void init()
    {

    }
    public void goBack(ActionEvent event)
    {
        try{
            // display user interface
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/AttackerSetup.fxml"));
            Parent root = loader.load();
            AttackerSetupController UI = loader.getController();
            UI.init();
            Controller.newWindow(root);

            Stage stage = (Stage) disconnect.getScene().getWindow();
            stage.close();
            attack_socket.close();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
