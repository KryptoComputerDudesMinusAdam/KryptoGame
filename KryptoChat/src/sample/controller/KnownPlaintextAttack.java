package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import sample.model.Message;

import java.io.*;

public class KnownPlaintextAttack extends AttackerSetupController
{
    @FXML
    Button disconnect, query_server, run_analysis, copy_message;

    @FXML
    ListView<String> ciphertext, plaintext, response;

    public void init() throws IOException
    {
        //getMessages();
    }

    private void getMessages() throws IOException, ClassNotFoundException
    {
        objis = new ObjectInputStream((InputStream) attack_socket.getInputStream());
        objos = new ObjectOutputStream((OutputStream) attack_socket.getOutputStream());

        while(true)
        {
            Message receieve = (Message)objis.readObject();
        
        }
    }

    public void goBack(ActionEvent event)
    {
        try{
            // display user interface
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/AttackerSetup.fxml"));
            Parent root = loader.load();
            AttackerSetupController UI = loader.getController();
            UI.init(Controller.newWindow(root));
            Stage stage = (Stage) disconnect.getScene().getWindow();
            stage.close();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
