package sample.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import sample.model.*;
import java.io.IOException;

public class CiphertextOnlyAttack extends AttackerSetupController
{
    int counter=0;
    String[] result;
    Conversation con;
    @FXML
    Button disconnect, queryCipheretext, runAnalysis;

    @FXML
    ListView<String> cipherList;

    @FXML
    ObservableList<String> mess = FXCollections.observableArrayList();
    @FXML
    TextArea fq, key;
    @Override
    public void init() throws IOException
    {
        runAnalysis.setDisable(true);
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

            objos.close();
            objis.close();
            attack_socket.close();
            attack_socket = null;
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    //Run analysis on ciphertext
    public void runAnalysis(ActionEvent actionEvent)
    {
        fq.setText(result[0]);
        key.setText(result[1]);
        this.runAnalysis.setDisable(true);
    }

    //Query the server for ciphertext
    public void query(ActionEvent actionEvent) throws IOException, ClassNotFoundException
    {
        AnalyzeThread at = new AnalyzeThread();
        if(counter < 1)
        {
            Message m = new Message("AttackerCiphertextOnly");
            objos.writeObject(m);
            Object o = objis.readObject();
            if(o instanceof Conversation){
                con = (Conversation)o;
            if(con.msgs.size()!=0){
                for(int i = 0; i < con.msgs.size();i++ )
                    mess.add(con.msgs.get(i).encryptedMessage);
                cipherList.setItems(mess);
                at.start();
                counter++;
            }}
        }

        if(counter>=1)
            queryCipheretext.setDisable(true);

    }

    class AnalyzeThread extends Thread
    {
        @Override
        public void run()
        {
            new Thread(()->{
                if(!mess.isEmpty() && !con.isEmpty())
                {
                    StringBuffer bf = new StringBuffer();
                    for(int i = 0; i < cipherList.getItems().size(); i++)
                        bf.append(". "+cipherList.getItems().get(i));
                    switch (con.typeOfEncryption.toLowerCase())
                    {
                        case "monoalphabetic":
                            FrequencyAnalysis f = new FrequencyAnalysis();
                            f.analyze(((bf.toString()).replaceAll("[^a-zA-Z]]","")).toUpperCase());
                            result = f.printMaps().split("#");
                            Platform.runLater(() -> {
                                runAnalysis.setDisable(false);
                            });
                            break;
                        case "stream":
                            break;
                        case "vigenere":
                            break;
                        default:
                            break;
                    }
                }else {

                }
            }).start();
        }
    }

}
