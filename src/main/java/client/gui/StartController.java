package client.gui;


import javafx.scene.layout.StackPane;
import net.Protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class StartController implements Initializable {

    @FXML
    private TextField ipField;
    @FXML
    private TextField nameField;
    @FXML
    private StackPane welcomePane;

    private static Logger logger = LogManager.getRootLogger();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameField.textProperty().addListener(((observable, oldValue, newValue) -> {
            String next =
                newValue.replaceAll(" ", "_").replaceAll(Protocol.DELIMITER, "");
            nameField.setText(next);
        }));
        welcomePane.setStyle(
            "-fx-background-image: url('/client/sprites/WelcomeBackground.png');" +
                "-fx-background-repeat: no-repeat;" + "-fx-background-size: cover;" +
                "-fx-background-position: center center;" +
                "-fx-background-size: stretch");
        logger.info("Initialized start scene");
    }

    /**
     * set the text of the server address field
     */
    public void setIpField(String ip) {
        ipField.setText(ip);
    }

    /**
     * set the text of the username field
     */
    public void setNameField(String name) {
        nameField.setText(name);
    }

    /**
     * switches to the lounge scene, attemting to start a connection
     */
    public void switchToLounge() {
        try {
            GUI.launchClientSocket(ipField.getText(), nameField.getText());
            GUI.getApplication().showLoungeScene(false);
            logger.info("Connected to server, switching to lounge");
        } catch (IOException e) {
            logger.warn("Error establishing connection: ", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setContentText("No server found at " + ipField.getText());
            alert.showAndWait();
        }
    }
}
