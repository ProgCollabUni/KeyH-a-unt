package client;

import net.Protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import client.gui.GameController;
import client.gui.SceneController;

public class ClientTasks {
    private static ClientSocketThread cparent;

    private SceneController sceneController;

    private GameController gameController;

    private GameTimer timer;

    private long millis;

    /**
     * creates a new tasks instance
     *
     * @param thread associated client socket thread
     */
    public ClientTasks(ClientSocketThread thread) {
        cparent = thread;
    }

    private static Logger logger = LogManager.getRootLogger();

    /**
     * Set the Controller to the main SceneController.
     */
    public void setSceneController(SceneController control) {
        this.sceneController = control;
    }

    /**
     * Sets gameController to the main GameController.
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        timer = new GameTimer(cparent, gameController);
    }

    /**
     * handles an incoming transmission
     *
     * @param cmd  the decoded command
     * @param args the entire transmission
     */
    public void receive(Protocol cmd, String args) {
        logger.trace("received " + args);
        switch (cmd) {
            case BROADCAST ->
                printToChat("BROADCAST " + Protocol.getMessage(args), "broadcast");
            case GLOBALCHAT -> printToChat(Protocol.getMessage(args), "global");
            case CHANGENICKNAME -> changeChatLabel(Protocol.getMessage(args));
            case DISPLAY -> info(Protocol.getMessage(args));
            case PONG -> pong(Protocol.getMessage(args));
            case PING -> cparent.send(Protocol.PONG, Protocol.getMessage(args));
            case LOBBYCHAT -> printToChat(Protocol.getMessage(args), "lobby");
            case ROOMINFO -> roomInfo(args);
            case REQUESTACTION -> requestAction();
            case TOGGLEREADY ->
                sceneController.updateReadyStatus(Protocol.getMessage(args));
            case STARTGAME -> sceneController.startGame();
            case ENDGAME -> sceneController.endGame();
            case DISCONNECT -> cparent.disconnect();
            case ERROR -> error(Protocol.getMessage(args));
            case DISPLAYLOBBY -> displayList("lobbies", Protocol.getMessage(args));
            case PLAYERLIST -> displayList(Protocol.getParameters(args, 1)[1],
                Protocol.getParameters(args, 1)[2]);
            case GAMELIST -> displayList("games", Protocol.getMessage(args));
            case WHISPER -> printToChat(Protocol.getMessage(args), "whisper");
            case WHISPERLIST -> sceneController.whisperList(Protocol.getMessage(args));
            case GAMEINFO -> gameInfo(Protocol.getMessage(args));
            case HIGHSCORES -> highScores(Protocol.getParameters(args, 3));
            case TARGETDIRECTION -> updateDirection(Protocol.getMessage(args));
            case ROOMPLAYER -> displayRoomPlayer(Protocol.getMessage(args));
            case PLAYERINFO -> playerInfo(Protocol.getMessage(args));
            default -> logger.warn("Client error with message: " +
                    args + " Command not found");
        }
    }

    /**
     * writes a message to out
     */
    public void printToChat(String msg, String type) {
        if (sceneController != null) {
            sceneController.printInChat(msg, type);
        }
    }

    /**
     * Updates the directionLabel
     * @param dir f.ex. NORTH EAST
     */
    public void updateDirection(String dir) {
        gameInfo("Your target is: " + dir);
        gameController.updateDirection(dir);
    }

    /**
     * Displays the extra player if multiple people are in the same room
     * @param info "Ghost" or "Hunter"
     */
    public void displayRoomPlayer(String info) {
        gameController.setRoomPlayerInfo(info);
    }

    /**
     * Used if the server sends information about the game
     */
    public void gameInfo(String str) {
        gameController.displayInfo(str);
    }

    /**
     * Used if the server sends the roleInfo at the start of the game
     * @param role String, either "ghost" or "hunter"
     */
    public void playerInfo(String role) {
        gameController.setRole(role);
    }

    /**
     * Used in the ping-method to set the current Milliseconds, to measure
     * the ping
     */
    public void setMillis() {
        this.millis = System.currentTimeMillis();
    }

    /**
     * changes Chat Label to given input String
     */
    public void changeChatLabel(String string) {
        if (sceneController != null) {
            sceneController.changeChatLabel(string);
        }
    }

    /**
     * resets the missed pings
     */
    public void pong(String args) {
        if (args.equals("manual")) {
            long difference = System.currentTimeMillis() - millis;
            sceneController.info("Your current ping is: " + difference + " ms");
            logger.info("received pong from manual Ping");
        }
        cparent.resetMissedPings();
    }

    /**
     * Display a List in the list-panel, depending on the type
     */
    public void displayList(String type, String list) {
        logger.trace(type + ": " + list);
        if (sceneController != null) {
            sceneController.printInListTab(type, list);
        }
    }

    /**
     * prints info about the room the player is in
     */
    public void roomInfo(String args) {
        String[] params = Protocol.getParameters(args, 2);
        String[] doors = params[1].split(",");
        String[] chest = params[2].split(",");
        if (gameController != null) {
            gameController.setRoomDoorInfo(doors);
            gameController.setRoomChestInfo(chest);
            gameController.updateRoomDoors();
        }

    }

    /**
     * lets the player know that it's their turn
     */
    public void requestAction() {
        timer.reset();
        timer.start();
    }

    /**
     * Used when printing an error message to an error-Alert-Box
     */
    public void error(String args) {
        logger.error(args);
        sceneController.error(args);
    }

    /**
     * Similar to error-Method, but only gives Info-PopUps
     */
    public void info(String args) {
        logger.info(args);
        if (sceneController != null) {
            sceneController.info(args);
        }
    }

    /**
     * displays the server's saved highscores
     */
    public void highScores(String[] scores) {
        String out = "High scores:" + System.lineSeparator();
        for (int i = 1; i < scores.length; i++) {
            out += scores[i] + System.lineSeparator();
        }
        info(out);
    }

    /**
     * stops the timer if a move is submitted
     */
    public void cancelTimer() {
        timer.stop();
    }

}
