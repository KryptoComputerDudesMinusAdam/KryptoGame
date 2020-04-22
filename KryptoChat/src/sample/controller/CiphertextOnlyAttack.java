package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
import sample.model.Conversation;
import sample.model.Message;

import java.io.IOException;

public class CiphertextOnlyAttack extends AttackerSetupController
{
    int counter=0;
    @FXML
    Button disconnect, queryCipheretext, runAnalysis;

    @FXML
    ListView<String> cipherList;

    @FXML
    ObservableList<String> mess = FXCollections.observableArrayList();

    @Override
    public void init() throws IOException
    {
        this.cipherList.setItems(this.mess);
        this.cipherList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    // Disconnect
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

    //Run analysis on ciphertext
    public void runAnalysis(ActionEvent actionEvent) throws IOException {
        init();
    }

    //Query the server for ciphertext
    public void query(ActionEvent actionEvent) throws IOException, ClassNotFoundException
    {
        if(counter < 1)
        {
            Message m = new Message("AttackerCiphertextOnly");
            objos.writeObject(m);
            Conversation con = (Conversation) objis.readObject();
            for(int i = 0; i < con.msgs.size();i++ )
                mess.add(con.msgs.get(i).encryptedMessage);
            cipherList.setItems(mess);
            counter++;
        }
        if(counter>=1)
            queryCipheretext.setDisable(true);
    }
}
