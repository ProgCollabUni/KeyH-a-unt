package client.gui;

import client.ClientSocketThread;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javafx.util.Duration;

import net.Protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    private static Logger logger = LogManager.getRootLogger();
    private final ClientSocketThread csocketthread = GUI.getSocketThread();
    private String role;
    private boolean hasMoved;
    private Pane[] otherPlayers;
    @FXML
    AnchorPane gamePane;
    @FXML
    GridPane gridPane;
    @FXML
    Pane chestPane = new Pane();
    @FXML
    Button northButton = new Button();
    @FXML
    Button eastButton = new Button();
    @FXML
    Button southButton = new Button();
    @FXML
    Button westButton = new Button();
    @FXML
    Button actionButton;
    @FXML
    Label timer;
    @FXML
    Label infoBox;
    @FXML
    Pane player1;
    @FXML
    Pane player2;
    @FXML
    Pane player3;
    @FXML
    Pane player4;
    @FXML
    Label directionLabel;
    String roomPlayerInfo = "false" + Protocol.DELIMITER + "0";
    String[] roomDoorInfo = new String[0];
    String[] roomChestInfo = new String[0];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        csocketthread.getClientTasks().setGameController(this);
        // Initialize DirectionLabel
        directionLabel.setStyle(Utilities.arrowStyle);
        directionLabel.setVisible(false);
        this.otherPlayers = new Pane[] {player2, player3, player4};
        logger.trace("Switched to game scene");

        // Load Background Image
        gridPane.setStyle(Utilities.roomStyle);
    }

    /**
     * Sets the role of the player at the beginning of the game
     *
     * @param role "ghost" or "hunter"
     */
    public void setRole(String role) {
        this.role = role;
        if (role.equals("ghost")) {
            player1.setStyle(Utilities.ghostStyle);
            actionButton.setVisible(false);

        } else {
            player1.setStyle(Utilities.playerStyle);
            actionButton.setVisible(true);
        }
    }

    /**
     * Updates the directionLabel with the arrow pointing in the right direction
     * Default direction is "EAST"
     */
    public void updateDirection(String dir) {
        directionLabel.setVisible(true);
        try {
            directionLabel.setRotate(Utilities.getRotation(dir));
        } catch (IllegalArgumentException e) {
            directionLabel.setVisible(false);
            logger.info("updateDirection: already at target");
        }
    }

    /**
     * Sets the local roomPlayerInfo with the current configuration from the server
     */
    public void setRoomPlayerInfo(String roomPlayerInfo) {
        this.roomPlayerInfo = roomPlayerInfo;
    }

    /**
     * Sets the local roomDoorInfo with the current configuration from the server
     */
    public void setRoomDoorInfo(String[] roomDoorInfo) {
        this.roomDoorInfo = roomDoorInfo;
    }

    /**
     * Sets the local roomChestInfo with the current configuration from the server
     */
    public void setRoomChestInfo(String[] roomChestInfo) {
        this.roomChestInfo = roomChestInfo;
    }

    /**
     * Displays the other players in the room in the GUI
     */
    public void updateRoomPlayers() {
        String info = this.roomPlayerInfo;
        for (Pane p : this.otherPlayers) {
            p.setVisible(false);
            p.setStyle(Utilities.playerStyle);
        }
        String[] roomInfo = info.split(Protocol.DELIMITER, 2);
        int count = Integer.parseInt(roomInfo[1]);

        for (int i = 0; i < count; i++) {
            otherPlayers[i].setVisible(true);
        }

        if (roomInfo[0].equals("true")) {
            player2.setStyle(Utilities.ghostStyle);
        }
        logger.info("Displaying other Players in the room");
    }

    /**
     * Send game move command NORTH
     */
    public void goNorth() {
        csocketthread.send(Protocol.GAMEACTION, "MOVE~NORTH");
        displayInfo("You moved north!");
        csocketthread.getClientTasks().cancelTimer();
        hasMoved = true;
    }

    /**
     * Send game move command EAST
     */
    public void goEast() {
        csocketthread.send(Protocol.GAMEACTION, "MOVE~EAST");
        displayInfo("You moved east!");
        csocketthread.getClientTasks().cancelTimer();
        hasMoved = true;
    }

    /**
     * Send game move command SOUTH
     */
    public void goSouth() {
        csocketthread.send(Protocol.GAMEACTION, "MOVE~SOUTH");
        displayInfo("You moved south!");
        csocketthread.getClientTasks().cancelTimer();
        hasMoved = true;
    }

    /**
     * Send game move command WEST
     */
    public void goWest() {
        csocketthread.send(Protocol.GAMEACTION, "MOVE~WEST");
        displayInfo("You moved west!");
        csocketthread.getClientTasks().cancelTimer();
        hasMoved = true;
    }

    /**
     * Action button for players.
     * Hunter loots chest
     * Ghost gets the position of the next player
     */
    public void roomAction() {
        csocketthread.send(Protocol.GAMEACTION, "ACTION");
        csocketthread.getClientTasks().cancelTimer();
    }

    /**
     * Sets doors invisible at the beginning of the players turn.
     * <p>
     * doors is a string array containing information about the room doors.
     */
    public void updateRoomDoors() {
        String[] doors = this.roomDoorInfo;
        Platform.runLater(() -> {

            if (hasMoved) {
                hasMoved = false;
                FadeTransition ft = new FadeTransition(Duration.millis(500), gridPane);
                ft.setFromValue(1.0);
                ft.setToValue(0.0);
                ft.setAutoReverse(false);

                FadeTransition ft2 = new FadeTransition(Duration.millis(500), gridPane);
                ft2.setFromValue(0.0);
                ft2.setToValue(1.0);

                ft.setOnFinished(finish -> {
                    updateRoomChest();
                    updateRoomPlayers();
                    changeRoomDoors(doors);
                    ft2.play();
                });

                ft.play();
            } else {
                updateRoomChest();
                updateRoomPlayers();
                changeRoomDoors(doors);
            }

        });
    }

    /**
     * Responsible for updating the room doors.
     */
    public void changeRoomDoors(String[] doors) {
        Platform.runLater(() -> {

            northButton.setVisible(false);
            southButton.setVisible(false);
            eastButton.setVisible(false);
            westButton.setVisible(false);
            if (role.equals("ghost")) {
                northButton.setVisible(true);
                northButton.setStyle(Utilities.ghostDoor);
                southButton.setVisible(true);
                southButton.setStyle(Utilities.ghostDoor);
                eastButton.setVisible(true);
                eastButton.setStyle(Utilities.ghostDoor);
                westButton.setVisible(true);
                westButton.setStyle(Utilities.ghostDoor);
            }
            for (String door : doors) {
                if (Objects.equals(door, "NORTH")) {
                    northButton.setStyle(Utilities.northDoor);
                    northButton.setVisible(true);
                }
                if (Objects.equals(door, "SOUTH")) {
                    southButton.setStyle(Utilities.southDoor);
                    southButton.setVisible(true);
                }
                if (Objects.equals(door, "EAST")) {
                    eastButton.setStyle(Utilities.eastDoor);
                    eastButton.setVisible(true);
                }
                if (Objects.equals(door, "WEST")) {
                    westButton.setStyle(Utilities.westDoor);
                    westButton.setVisible(true);
                }
            }
        });
    }

    /**
     * Sets full or empty chest ingame;
     */
    public void updateRoomChest() {
        String[] chest = this.roomChestInfo;
        Platform.runLater(() -> {
            if (chest.length != 2) {
                chestPane.setVisible(false);
            } else {
                if (chest[1].equals("EMPTY")) {
                    chestPane.setStyle(Utilities.emptyChest);
                    chestPane.setVisible(true);
                }
                if (chest[1].equals("FULL")) {
                    chestPane.setStyle(Utilities.fullChest);
                    chestPane.setVisible(true);
                }
            }
        });

    }

    /**
     * Sets the countdown timer on GUI
     *
     * @param time in seconds
     */
    public void setTimer(int time) {
        Platform.runLater(() -> {
            timer.setText("Timer: " + time);
            if (time < 10) {
                timer.setTextFill(Color.RED);
            } else {
                timer.setTextFill(Color.BLACK);
            }
        });
    }

    /**
     * Displays information: If it's a players turn
     *
     * @param str contains the information.
     */
    public void displayInfo(String str) {
        Platform.runLater(() -> infoBox.setText(str));
    }

}
