package sample.controller;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Controller {

    public static void newWindow(Parent root){
        // New window (Stage)
        Scene secondScene = new Scene(root);
        Stage newWindow = new Stage();
        newWindow.setResizable(false);
        newWindow.setScene(secondScene);
        newWindow.setX(newWindow.getX() + 25);
        newWindow.setY(newWindow.getY() + 25);
        newWindow.show();
    }
}
