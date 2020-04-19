package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class MenuController {
    @FXML
    Button serverButton, clientButton, attackerButton;


    // Server button
    public void handleServerButton(ActionEvent event){
        try{
            // display user interface
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/Server.fxml"));
            Parent root = loader.load();
            ServerController UI = loader.getController();
            Controller.newWindow(root);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    // Client
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

    // Attacker
    public void handleAttackerButton(ActionEvent event)
    {
        try{
            // display user interface
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/AttackerSetup.fxml"));
            Parent root = loader.load();
            AttackerSetupController UI = loader.getController();
            UI.init();
            Controller.newWindow(root);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
