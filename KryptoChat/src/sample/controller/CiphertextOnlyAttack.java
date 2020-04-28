package sample.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sample.model.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class CiphertextOnlyAttack extends AttackerSetupController implements Initializable
{
    private static boolean found = false;
    public static String gen_key;
    public static String masterKey;
    boolean selected = false;
    BruteForce work = new BruteForce();
    int counter=0;
    String[] result;
    Conversation con;

    @FXML
    Button disconnect, queryCipheretext, runAnalysis,bruteforce;
    @FXML
    ListView<String> cipherList;
    @FXML
    ObservableList<String> mess = FXCollections.observableArrayList();
    @FXML
    TextArea fq, key;
    @FXML
    ProgressIndicator progress;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        if(selected){
        Platform.runLater(()->{
            progress.progressProperty().bind(work.progressProperty());
            new Thread(work).start();
        });
        }
    }
    @Override
    public void init() throws IOException
    {
        runAnalysis.setDisable(true);
        bruteforce.setDisable(true);
        progress.setProgress(0);
        this.cipherList.setItems(this.mess);
        this.cipherList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    // Disconnect
    public void goBack(ActionEvent event)
    {
        try{
            // display user interface
            work.cancel(true);
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

    //Run analysis on ciphertext
    public void runAnalysis(ActionEvent actionEvent)
    {
        masterKey = Cipher.generateMonoKey();
        selected = true;
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
            Message m = new Message("");
            m.from = "AttackerCiphertextOnly";
            objos.writeObject(m);
            Object o = objis.readObject();
            if(o instanceof Conversation && o != null){
                con = (Conversation)o;
                for(int i = 0; i < con.msgs.size();i++ )
                    mess.add(con.msgs.get(i).message);
                cipherList.setItems(mess);
                at.start();
                counter++;
            }else {
                Alert alert = new Alert(Alert.AlertType.WARNING,"\nNo Conversation to Intercept!", ButtonType.OK);
                alert.show();
            }
        }

        if(counter>=1)
            queryCipheretext.setDisable(true);

    }

    public void bruteforce(ActionEvent actionEvent) {
        initialize(null,null);
    }

    static class BruteForce extends Task<Integer>
    {
        @Override
        protected Integer call() throws Exception
        {
            int max = 10;
            for(int i = 0; !found && i < max; i++)
            {
                updateProgress(i+1,max);
                CiphertextOnlyAttack.gen_key = Cipher.generateMonoKey();
                Thread.sleep(500);
                if (CiphertextOnlyAttack.gen_key.equalsIgnoreCase(CiphertextOnlyAttack.masterKey)){
                    found=true;
                    return max;
                }
            }
            return max;
        }

        @Override
        public boolean cancel(boolean b) {
            return super.cancel(b);
        }

        @Override
        protected void updateProgress(double workdone, double max)
        {
            super.updateProgress(workdone,max);
        }
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
                        bf.append(" "+cipherList.getItems().get(i));
                    switch (con.typeOfEncryption.toLowerCase())
                    {
                        case "monoalphabetic":
                            FrequencyAnalysis f = new FrequencyAnalysis();
                            f.analyze((((bf.toString()).replaceAll("[^a-zA-Z]","")).toUpperCase()));
                            result = (f.printMaps()).split("#");
                            Platform.runLater(() -> {
                                runAnalysis.setDisable(false);
                                bruteforce.setDisable(false);
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
