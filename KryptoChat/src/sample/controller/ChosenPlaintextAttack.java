package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import sample.model.Message;

import java.io.IOException;

public class ChosenPlaintextAttack extends AttackerSetupController
{
    @FXML
    Button disconnect, query_encryption, save;

    @FXML
    TextArea plaintext, ciphertext;

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

    public void queryDecryption(ActionEvent actionEvent)
    {
        try {
            Message m = new Message("AttackerChosePlaintext");
            objos.writeObject(m);
            Message p = new Message(plaintext.getText());
            objos.writeObject(p);

            new Thread(()->{
                System.out.println("*** in thread");
                while(true){
                    try {
                        Message e = (Message) objis.readObject();
                        System.out.println("found messg: "+e.encryptedMessage);
                        ciphertext.setText(e.encryptedMessage);
                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(ActionEvent actionEvent)
    {

    }
}
