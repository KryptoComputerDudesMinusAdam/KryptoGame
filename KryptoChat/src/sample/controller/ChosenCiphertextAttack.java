package sample.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import sample.model.Message;

import java.io.IOException;

public class ChosenCiphertextAttack extends AttackerSetupController
{
    @FXML
    Button disconnect, query_decryption, save;

    @FXML
    TextArea encrypted, decrypted;

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
            attack_socket = null;
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void queryDecryption(ActionEvent actionEvent) {
        try {
            Message p = new Message(encrypted.getText());
            objos.writeObject(p);
            System.out.println("*** in thread");
            listenIn();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenIn(){
        new Thread(()->{
            while(!attack_socket.isClosed()) {
                try {
                    Message e = (Message) objis.readObject();
                    System.out.println("found messg: "+e.encryptedMessage);
                    Platform.runLater(() -> {
                        decrypted.setText(e.encryptedMessage);
                    });
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }

        }).start();
    }

    public void save(ActionEvent actionEvent) {
    }
}
