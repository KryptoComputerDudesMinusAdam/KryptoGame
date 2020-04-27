package sample.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import sample.model.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KnownPlaintextAttack extends AttackerSetupController
{
    @FXML
    Button disconnect, queryServer, run_analysis;

    @FXML
    ListView<String> response;

    @FXML
    ListView<Message> ciphertext, plaintext;
    List<Message> obsvE = new ArrayList<>();
    List<Message> obsvP = new ArrayList<>();

    public void init() throws IOException
    {
        System.out.println("Initializing UI");
        Controller.initializeListView(obsvE, ciphertext);
        Controller.initializeListView(obsvP, plaintext);
        Message m = new Message();
        m.from = "AttackerKnown-Plaintext";
        objos.writeObject(m);
    }

    public void query(ActionEvent event){
        System.out.println("after first write");
        listenIn();
    }

    public void listenIn(){
        new Thread(()->{
            try {
                if(!socketClosed){
                    for(int i = 0; i < 2; i++){
                        System.out.println("Waiting for read object");
                        Message e = (Message) objis.readObject();
                        System.out.println("found messg: "+e.message);
                        Platform.runLater(() -> {
                            if(e.isEncrypted){
                                System.out.println("enc m");
                                obsvE.add(e);
                                ciphertext.getItems().setAll(obsvE);
                                ciphertext.refresh();
                            } else{
                                System.out.println("dec m");
                                obsvP.add(e);
                                plaintext.getItems().setAll(obsvP);
                                ciphertext.refresh();
                            }
                        });
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void goBack(ActionEvent event) {
        try{
            // display user interface
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/AttackerSetup.fxml"));
            Parent root = loader.load();
            AttackerSetupController UI = loader.getController();
            Controller.newWindow(root);
            UI.init();
            Stage stage = (Stage) disconnect.getScene().getWindow();
            stage.close();

            socketClosed = true;
            objos.close();
            objis.close();
            attack_socket.close();
            attack_socket = null;
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
