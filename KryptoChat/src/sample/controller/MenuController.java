package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class MenuController {
    @FXML
    Button serverButton, clientButton, attackerButton;

    public void handleServerButton(ActionEvent event){
        /*
            TODO:
                view server window
         */
    }

    public void handleClientButton(ActionEvent event){
        try{
            // display user interface
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/ClientSetup.fxml"));
            Parent root = loader.load();
            ClientSetupController UI = loader.getController();
            Controller.newWindow(root);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void handleAttackerButton(ActionEvent event){
        /*
            TODO:
                view attacker window
         */
    }
}
