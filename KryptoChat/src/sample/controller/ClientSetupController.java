package sample.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ClientSetupController {

    @FXML
    Button serverButton, receiverButton;
    @FXML
    TextField hostnameTextField, serverTextField, clientNameTextField;
    @FXML
    ComboBox<String> encryptionComboBox;
    @FXML
    ListView<String> contactsListView;

    public void handleServerButton(ActionEvent event){
        if(hostnameTextField.getText() != null &&
                serverTextField.getText() != null &&
                clientNameTextField.getText() != null){
            new ClientThread(hostnameTextField.getText(), Integer.parseInt(serverTextField.getText())).start();
        }
    }

    public void handleReceiverButton(ActionEvent event){
        try{
            // display user interface
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../view/ClientChatRoom.fxml"));
            Parent root = loader.load();
            ClientChatRoomController UI = loader.getController();
            Controller.newWindow(root);
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}

class ClientThread extends Thread {
    private String host;
    private int port;

    public ClientThread(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void run(){
        try(Socket socket = new Socket(host, port)) {
            while(true){
                // keep client running while server is on
                // TODO: catch when server is off and exit app
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}