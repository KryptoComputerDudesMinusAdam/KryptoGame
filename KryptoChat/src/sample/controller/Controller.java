package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import sample.model.Message;

import java.util.List;

public class Controller {

    public static Stage newWindow(Parent root){
        // New window (Stage)
        Scene secondScene = new Scene(root);
        Stage newWindow = new Stage();
        newWindow.setResizable(false);
        newWindow.setScene(secondScene);
        newWindow.setX(newWindow.getX() + 25);
        newWindow.setY(newWindow.getY() + 25);
        newWindow.show();

        return newWindow;
    }

    public static void initializeListView(List<Message> messages, ListView<Message> listView){
        ObservableList<Message> observableList = FXCollections.observableArrayList(messages);
        listView.setItems(observableList);
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null || message.encryptedMessage == null) {
                    setText(null);
                } else {
                    setText(message.encryptedMessage);
                }
            }
        });
    }
}
