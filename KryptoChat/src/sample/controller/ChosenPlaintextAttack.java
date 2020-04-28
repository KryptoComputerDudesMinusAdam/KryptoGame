package sample.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sample.model.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChosenPlaintextAttack extends AttackerSetupController
{
    @FXML
    Button disconnect, queryEncryption;

    @FXML
    TextArea plaintext;

    @FXML
    ListView<Message> ciphertext;
    List<Message> list = new ArrayList<>();
    private int currQueries = 0;

    public void init() throws IOException
    {
        System.out.println("Initializing UI");
        Controller.initializeListView( list, ciphertext);
        Message m = new Message();
        m.from = "AttackerChosePlaintext";
        objos.writeObject(m);
        listenIn();
    }

    public void goBack(ActionEvent event) {
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

            socketClosed = true;
            objos.close();
            objis.close();
            attack_socket.close();
            attack_socket = null;
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void queryEncryption(ActionEvent actionEvent) {
        int maxQueries = 5;
        if(currQueries< maxQueries) {
                try {
                    Message p = new Message(plaintext.getText());
                    System.out.println("Sending: " + plaintext.getText());
                    objos.writeObject(p);
                    System.out.println("*** in thread");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currQueries++;
        } else{
            Alert alert = new Alert(Alert.AlertType.ERROR, "You have reached your max amount of queries", ButtonType.OK);
            alert.showAndWait();
        }
    }


    private void listenIn() {
        new Thread(()->{
            while(!socketClosed) {
                System.out.println("CHECK: "+socketClosed);
                try {
                    Message e = (Message) objis.readObject();
                    System.out.println("found messg: "+e.message);
                    Platform.runLater(() -> {
                        list.add(e);
                        ciphertext.getItems().setAll(list);
                        ciphertext.refresh();
                    });
                } catch (IOException | ClassNotFoundException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }).start();
    }
}
