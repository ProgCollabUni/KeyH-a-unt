package client.gui;

import javafx.scene.paint.Color;

/**
 * Utility class to keep GUI controllers cleaner
 */
public class Utilities {

    /**
     * CSS-style inject for the fullChest sprite
     */
    public static String fullChest = "-fx-background-image:  " +
        "url('/client/sprites/ChestFull.png'); " +
        "-fx-background-repeat: no-repeat;" +
        "-fx-background-size: contain;" +
        "-fx-background-position: center center";

    /**
     * CSS-style inject for the emptyChest sprite
     */
    public static String emptyChest = "-fx-background-image:  " +
        "url('/client/sprites/ChestEmpty.png'); " +
        "-fx-background-repeat: no-repeat;" +
        "-fx-background-size: contain;" +
        "-fx-background-position: center center";

    /**
     * CSS-style inject for the room sprite
     */
    public static String arrowStyle =
        "-fx-background-image: url('/client/sprites/Arrow.png');" +
            "-fx-background-repeat: no-repeat;" + "-fx-background-size: cover;" +
            "-fx-background-position: center center";

    /**
     * CSS-style inject for the room sprite
     */
    public static String roomStyle =
        "-fx-background-image: url('/client/sprites/DefaultRoom.gif');" +
            "-fx-background-repeat: no-repeat;" + "-fx-background-size: cover;" +
            "-fx-background-position: center center;" +
            "-fx-background-size: stretch";

    /**
     * CSS-style inject for the ghost sprite
     */
    public static String ghostStyle =
        "-fx-background-image: url('/client/sprites/TempGhost.gif');" +
            "-fx-background-repeat: no-repeat;" + "-fx-background-size: contain;" +
            "-fx-background-position: center center";
    /**
     * CSS-style inject for the player sprite
     */
    public static String playerStyle =
        "-fx-background-image: url('/client/sprites/TempPlayer.gif');" +
            "-fx-background-repeat: no-repeat;" +
            "-fx-background-size: contain;" +
            "-fx-background-position: center center";
    /**
     * CSS-style inject for the ghost door.
     */
    public static String ghostDoor = "-fx-background-image: " +
            "url('/client/sprites/GhostDoor.gif');" +
            "-fx-background-size: stretch;" +
            "-fx-border-color: transparent; " +
            "-fx-background-color: transparent; " +
            "-fx-border-radius: 0";

    /**
     * CSS-style inject for the NORTH door.
     */
    public static String northDoor = "-fx-background-image: " +
        "url('/client/sprites/DoorNorth.png');" +
        "-fx-background-size: stretch;" +
        "-fx-background-repeat: no-repeat;" +
        "-fx-border-color: transparent; " +
        "-fx-background-color: transparent; " + "-fx-border-radius: 0";

    /**
     * CSS-style inject for the SOUTH door.
     */
    public static String southDoor = "-fx-background-image: " +
        "url('/client/sprites/DoorSouth.png');" +
        "-fx-background-size: stretch;" +
        "-fx-background-repeat: no-repeat;" +
        "-fx-border-color: transparent; " +
        "-fx-background-color: transparent; " + "-fx-border-radius: 0";

    /**
     * CSS-style inject for the EAST door.
     */
    public static String eastDoor = "-fx-background-image: " +
        "url('/client/sprites/DoorEast.png');" +
        "-fx-background-size: stretch;" +
        "-fx-background-repeat: no-repeat;" +
        "-fx-border-color: transparent; " +
        "-fx-background-color: transparent; " + "-fx-border-radius: 0";

    /**
     * CSS-style inject for the WEST door.
     */
    public static String westDoor = "-fx-background-image: " +
        "url('/client/sprites/DoorWest.png');" +
        "-fx-background-size: stretch;" +
        "-fx-background-repeat: no-repeat;" +
        "-fx-border-color: transparent; " +
        "-fx-background-color: transparent; " + "-fx-border-radius: 0";

    /**
     * @return counterclockwise rotation in degrees based on "Compass" directions
     * @throws IllegalArgumentException if there is no match
     */
    public static double getRotation(String direction) throws IllegalArgumentException {
        direction = direction.trim();
        switch (direction) {
            case "EAST" -> {
                return 0;
            }
            case "NORTH EAST", "EAST NORTH" -> {
                return 315;
            }
            case "NORTH" -> {
                return 270;
            }
            case "NORTH WEST", "WEST NORTH" -> {
                return 225;
            }
            case "WEST" -> {
                return 180;
            }
            case "SOUTH WEST", "WEST SOUTH" -> {
                return 135;
            }
            case "SOUTH" -> {
                return 90;
            }
            case "SOUTH EAST", "EAST SOUTH" -> {
                return 45;
            }
            default -> throw new IllegalArgumentException("No matching Entry!");

        }
    }

    /**
     * @return a JavaFX color for different Chat types
     */
    public static Color getChatColor(String type) {
        switch (type) {
            case "broadcast" -> {
                return Color.RED;
            }
            case "lobby" -> {
                return Color.BLACK;
            }
            case "global" -> {
                return Color.BLUE;
            }
            default -> {
                return Color.GREEN;
            }
        }
    }
}
