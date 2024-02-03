package client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;


public class ChatApp extends Application {

    /**
     * declaring stage for the chat app
     */
    public static Stage stage;

    private static Pane welcomePane;
    private static StartController startController;

    private static Pane loungePane;

    private static Logger logger = LogManager.getRootLogger();

    /**
     * Starts the GUI application. Is still under development.
     */
    public void start(Stage primaryStage) {
        List<String> args = getParameters().getRaw();
        stage = primaryStage;
        showWelcomeScene();
        stage.setTitle("Key H(a)unt");
        stage.show();
        startController.setIpField(args.get(0));
        startController.setNameField(args.get(1));
        logger.info("Opened welcome stage");

    }

    /**
     * switches the scene to the login/welcome window
     */
    public void showWelcomeScene() {
        if (welcomePane == null) {
            FXMLLoader loader = new FXMLLoader();
            URL xmlUrl = getClass().getResource("/client/welcomeScene/WelcomeStage.fxml");
            loader.setLocation(xmlUrl);
            try {
                welcomePane = loader.load();
            } catch (IOException e) {
                logger.error("Error loading Welcome Scene: ", e);
            }
            startController = loader.getController();
            stage.setScene(new Scene(welcomePane));
            stage.setResizable(false);
        } else {
            stage.getScene().setRoot(welcomePane);
        }
    }

    /**
     * switches to the lounge scene, allowing to keep the old pane with a parameter
     */
    public void showLoungeScene(boolean keepOldPane) {
        if (loungePane == null || !keepOldPane) {
            try {
                FXMLLoader loader = new FXMLLoader();
                URL xmlUrl =
                        getClass().getResource("/client/loungeScene/LoungeStage.fxml");
                loader.setLocation(xmlUrl);
                loungePane = loader.load();
            } catch (IOException e) {
                logger.error("Error loading lounge scene: ", e);
            }

        }
        stage.setResizable(true);
        stage.getScene().setRoot(loungePane);
        stage.setMaximized(true);
        stage.setMinHeight(400);
        stage.setMinWidth(600);
    }

}
