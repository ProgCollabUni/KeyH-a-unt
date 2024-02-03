package client.gui;

import client.ClientSocketThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.WindowEvent;

import javafx.util.Duration;
import net.Protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * SceneController controls the entire GUI
 */
public class SceneController implements Initializable {

    @FXML
    private AnchorPane lobbyPane;
    private AnchorPane gamePane;
    @FXML
    private AnchorPane chatPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private VBox chatLog = new VBox();
    @FXML
    private VBox lobbyList = new VBox();
    @FXML
    private ScrollPane scrollPaneChat;
    @FXML
    private ScrollPane scrollPaneLobby;
    @FXML
    private ChoiceBox<String> chatChoiceBox = new ChoiceBox<>();
    @FXML
    private TextField chatMsgInput;
    @FXML
    private Button readyButton;
    @FXML
    private Label chatLabel;
    @FXML
    private Label listLabel;
    @FXML
    private Menu joinMenu;
    @FXML
    private ContextMenu whisperList;
    @FXML
    private CheckMenuItem muteMusicMenu;

    MediaPlayer mediaPlayer;

    private static ClientSocketThread csocketthread;

    private static Logger logger = LogManager.getRootLogger();

    private final String[] chatChoiceOptions =
        {"Global chat", "Lobby chat", "Whisper", "Broadcast"};

    String chatChoice = "Global chat";

    boolean showWhisperNames;

    String message = "";

    /**
     * Initializing chatLog, chatChoiceBox and Client socket thread (csochetthread)
     */

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // To scroll auotomatically to the bottom if new Chat messages come...
        chatLog.heightProperty().addListener(
            (observable, oldValue, newValue) -> scrollPaneChat.setVvalue(
                (Double) newValue));

        lobbyList.heightProperty().addListener(
            (observable, oldValue, newValue) -> scrollPaneLobby.setVvalue(
                (Double) newValue));

        chatChoiceBox.getItems().addAll(chatChoiceOptions);
        chatChoiceBox.setOnAction(this::setCommand);
        csocketthread = GUI.getSocketThread();
        csocketthread.getClientTasks().setSceneController(this);
        ChatApp.stage.getScene().getWindow().setOnCloseRequest(
            (WindowEvent we) -> {
                csocketthread.disconnect();
                Platform.exit();
            });
        logger.info("Initialized chat-log, scroll-pane and chatChoiceBox");

        Media backgroundMusic = new Media(Objects.requireNonNull(
            getClass().getResource("/client/sounds/DefaultMusic.mp3")).toExternalForm());
        this.mediaPlayer = new MediaPlayer(backgroundMusic);
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.seek(Duration.ZERO);
            }
        });
        mediaPlayer.play();
        mediaPlayer.setVolume(0.5);
    }


    /**
     * Starts the game by swapping the previously loaded panes
     * the left side lobby pane gets swapped to the game pane
     */
    public void startGame() {
        FXMLLoader gameLoader =
            new FXMLLoader(getClass().getResource("/client/gameScene/GameScene.fxml"));
        try {
            this.gamePane = gameLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Platform.runLater(() -> {
            mainPane.getChildren().clear();
            mainPane.getChildren().add(menuBar);
            mainPane.getChildren().add(chatPane);
            mainPane.getChildren().add(gamePane);
            logger.info("Starting game by swapping to gamePane");
        });
    }

    /**
     * ends the Game by swapping the gamePane back to the lobbyPane
     */
    public void endGame() {
        Platform.runLater(() -> {
            mainPane.getChildren().clear();
            mainPane.getChildren().add(menuBar);
            mainPane.getChildren().add(chatPane);
            mainPane.getChildren().add(lobbyPane);
            logger.info("Ending game by swapping back to lobbyPane");
        });
    }

    /**
     * Prints every message received from Server in GlobalOut.
     */
    public void printInChat(String msg, String type) {
        Text text = new Text(msg);

        text.setFill(Utilities.getChatColor(type));
        text.setFont(Font.font("Arial", 20));
        TextFlow textFlow = new TextFlow(text);
        Platform.runLater(() -> chatLog.getChildren().add(textFlow));
    }

    /**
     * Gets the message on press of the "ENTER" key from the user. The Message gets
     * automatically send to the server with the corresponding protocol.
     */
    public void getMessage() {
        message = chatMsgInput.getText();
        switch (chatChoice) {
            case "Lobby chat" -> csocketthread.send(Protocol.LOBBYCHAT, message);
            case "Whisper" -> whisper(message);
            case "Broadcast" -> csocketthread.send(Protocol.BROADCAST, message);
            default -> csocketthread.send(Protocol.GLOBALCHAT, message);
        }
        logger.trace("Sending " + chatChoice + " , message is" + message);
        chatMsgInput.clear();
    }

    /**
     * Used to send a whisper Chat from the GUI to the client
     */
    private void whisper(String message) {
        String[] msg = message.split(" ", 2);
        if (msg.length < 2) {
            //-> Alert box oder so?
            logger.warn("Format needs to be: " +
                "<username> <message> (username has to be valid)");
        } else {
            String username = msg[0];
            String chat = msg[1];
            csocketthread.send(Protocol.WHISPER, username + Protocol.DELIMITER + chat);
            printInChat(">> " + username + ": " + chat, "whisper");
            logger.trace("Whisper to user: " + username + " with chat message: " + chat);
            showWhisperNames = true;
        }
    }

    /**
     * Lets the player choose between "Global chat", "Lobby chat", "Whisper chat".
     * Default is "Global chat"
     */
    private void setCommand(javafx.event.ActionEvent actionEvent) {
        chatChoice = chatChoiceBox.getValue();
        if (chatChoice.equals("Whisper")) {
            playerlistServer();
            showWhisperNames = true;
        }
    }

    /**
     * Used to log out
     */
    public void logout() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Logging out");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            csocketthread.send(Protocol.LOGOUT, "");
            GUI.getApplication().showWelcomeScene();
            logger.info("Client logged out via GUI");
        }
    }

    /**
     * Method used by other methods to print on the left side in the
     * lounge scene, f.ex. lobbylists, gamelists, playerlists, etc...
     */
    public void printInListTab(String type, String list) {
        Platform.runLater(() -> {
            listLabel.setText("");
            lobbyList.getChildren().clear();
            joinMenu.getItems().clear();
            if (!list.equals("")) {
                String[] items = list.split(Protocol.DELIMITER);
                for (String item : items) {
                    if (type.equals("lobbies")) {
                        MenuItem menuItem = new MenuItem(item);
                        menuItem.setOnAction(
                            t -> csocketthread.send(Protocol.JOINLOBBY, item));
                        joinMenu.getItems().add(menuItem);
                    }
                    Text text = new Text(item);
                    text.setFill(Color.BLACK);
                    text.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    TextFlow textFlow = new TextFlow(text);
                    lobbyList.getChildren().add(textFlow);
                }
                listLabel.setText("List: " + type);
                logger.trace("printing " + type + " list");
            }
        });
    }

    /**
     * Creates a lobby, by switching to the AlertBoxController
     * Work continues there.
     */

    public void createLobby() {
        String inputLobby = "";
        TextInputDialog newLobby = new TextInputDialog();
        newLobby.setTitle("Create Lobby");
        newLobby.setHeaderText(null);
        newLobby.setContentText(
            "What is the name of the lobby you'd like" + " to create?");

        newLobby.showAndWait();

        inputLobby = newLobby.getEditor().getText();
        csocketthread.send(Protocol.CREATELOBBY, inputLobby);
        refreshLobbies();
        logger.info("Client is trying to create new lobby " + inputLobby);
    }

    /**
     * Refreshes the lobbylist when the Refresh-Button is pressed
     */
    public void refreshLobbies() {
        csocketthread.send(Protocol.DISPLAYLOBBY, "");
        logger.trace("Client requested a fresh lobby list");
    }

    /**
     * Used when a key is typed in the text field -> New playerlist gets requested
     */
    public void refreshWhisperList() {
        if (chatChoice.equals("Whisper") && showWhisperNames) {
            csocketthread.send(Protocol.WHISPERLIST, "");
        }
        logger.trace("Client requested new whisper name list");
    }

    /**
     * displays the whisper name list (contextMenu)
     *
     * @param list names of playes on the server
     */
    public void whisperList(String list) {
        Platform.runLater(() -> {
            if (chatChoice.equals("Whisper") && showWhisperNames) {
                whisperList.getItems().clear();

                String[] players = list.split(Protocol.DELIMITER);
                for (int i = 0; i < players.length; i++) {
                    players[i] = players[i].substring(1, players[i].length());
                    players[i] = players[i].substring(0, players[i].length() - 1);
                }

                for (String p : players) {
                    MenuItem menuItem = new MenuItem(p);
                    menuItem.setOnAction(e -> {
                        chatMsgInput.clear();
                        chatMsgInput.setText(p + " ");
                        chatMsgInput.positionCaret(chatMsgInput.getText().length());
                        whisperList.hide();
                        showWhisperNames = false;
                    });
                    menuItem.setMnemonicParsing(false);
                    whisperList.getItems().add(menuItem);
                }
                whisperList.show(chatMsgInput, Side.BOTTOM, 0, 0);
            }
        });
    }

    /**
     * Used to display a list of players connected to the server
     */
    public void playerlistServer() {
        csocketthread.send(Protocol.PLAYERLIST, "Players in server");
        logger.trace("Client requested a fresh server player list");
    }

    /**
     * Used to display a list of players in the same lobby
     */
    public void playerlistLobby() {
        csocketthread.send(Protocol.PLAYERLIST, "Players in lobby");
        logger.trace("Client requested a lobby player list");
    }

    /**
     * Used when Button gets pressed to get a game list from the server
     */
    public void getGameList() {
        csocketthread.send(Protocol.GAMELIST, "");
        logger.trace("Client requested a game list");
    }

    /**
     * Gets called if user chooses ping-menu
     */
    public void ping() {
        csocketthread.getClientTasks().setMillis();
        csocketthread.send(Protocol.PING, "manual");
    }

    /**
     * used to change your Nickname when pressing the Button
     */
    public void changeNickname() {
        String nickname = "";
        TextInputDialog changeNick = new TextInputDialog();
        changeNick.setTitle("Change Nickname");
        changeNick.setHeaderText(null);
        changeNick.setContentText("What is the name of the nickname you'd " + "like?");

        changeNick.getEditor().textProperty()
            .addListener(((observable, oldValue, newValue) -> {
                String next =
                    newValue.replaceAll(" ", "_").replaceAll(Protocol.DELIMITER, "");
                changeNick.getEditor().setText(next);
            }));

        Optional<String> result = changeNick.showAndWait();
        if (result.isPresent()) {
            nickname = changeNick.getEditor().getText();
            csocketthread.send(Protocol.CHANGENICKNAME, nickname);
            logger.trace("Attempting to change nickname to " + nickname);
        }
    }

    /**
     * Sets ChatLabel to new Text given by "string"
     */
    public void changeChatLabel(String string) {
        Platform.runLater(() -> {
            chatLabel.setText("Chat - " + string);
        });
    }

    /**
     *
     */
    public void closeLobby() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Close Lobby");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure yu want to close the lobby?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            csocketthread.send(Protocol.CLOSELOBBY, "");
        }
    }

    /**
     * Button that allows client to leave a lobby.
     */
    public void leaveLobby() {
        csocketthread.send(Protocol.LEAVELOBBY, "");
        logger.info("Client left lobby");
    }

    /**
     * Toggles ready on the server side; used when readyButton gets pressed
     */
    public void setReady() {
        csocketthread.send(Protocol.TOGGLEREADY, "");
    }

    /**
     * Updates the ready-status in the GUI, depending on if you're ready
     * or not, the button looks different
     */
    public void updateReadyStatus(String ready) {
        if (ready.equals("true")) {
            readyButton.setStyle("-fx-background-color: Green");
        } else {
            readyButton.setStyle(null);
        }
    }

    /**
     * shows the Manual, when chosen in the menu
     */
    public void showManual() {
        InputStream pdfIn = getClass().getResourceAsStream(
            "/client/documents/Bedienungsanleitung_Key_Haunt.pdf");
        try {
            File tmp = new File("tmp_manual.pdf");
            FileOutputStream writer = new FileOutputStream(tmp);
            while (pdfIn.available() > 0) {
                writer.write(pdfIn.read());
            }
            writer.close();
            GUI.getApplication().getHostServices()
                .showDocument(String.valueOf(tmp.toURI()));
        } catch (IOException e) {
            logger.error("Error showing manual: ", e);
        }

    }

    /**
     * mutes the music when selected (CheckMenuItem)
     */
    public void muteMusic() {
        if (muteMusicMenu.isSelected()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }
    }

    /**
     * gets used, if there is an error message from the server; prints
     * the error message into an alert window
     */
    public void error(String args) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("The following error occured:");
            alert.setContentText(args);
            alert.showAndWait();
        });
    }

    /**
     * Similar to error method, put prints to an "Inforamtion-Window"
     */
    public void info(String args) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(args);
            alert.showAndWait();
        });
    }

    /**
     * request high-scores from the server
     */
    public void getHighScores() {
        csocketthread.send(Protocol.HIGHSCORES, "");
    }

}
