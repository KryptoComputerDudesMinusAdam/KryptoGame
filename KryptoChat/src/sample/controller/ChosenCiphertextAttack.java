package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ChosenCiphertextAttack extends AttackerSetupController
{
    @FXML
    Button disconnect;

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
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void queryDecryption(ActionEvent actionEvent) {
    }

    public void save(ActionEvent actionEvent) {
    }
}
