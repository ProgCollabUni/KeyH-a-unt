package player;

import net.Protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.ServerSocketThread;
import server.Lobby;

/**
 * Each client is a player, in this class we have his attributes
 */
public class Player {
    private String nickname = "";
    private boolean ready = false;
    private ServerSocketThread thread;

    private Lobby lobby = null;

    private static Logger logger = LogManager.getRootLogger();

    /**
     * constructor for a new player class
     */
    public Player(ServerSocketThread t) {
        this.thread = t;
        logger.trace("created new player");
    }

    /**
     * toggles the ready state, then checks if the whole lobby is ready
     */
    public void toggleReady() {
        ready = !ready;
        lobby.sendToAll(Protocol.PLAYERLIST,
            "Players in lobby" + Protocol.DELIMITER + lobby.printPlayers());
        this.thread.send(Protocol.TOGGLEREADY, String.valueOf(this.ready));
        lobby.lobbyReadyCheck();
    }

    /**
     * returns the ready status
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * lets the player have a nickname or change his old nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * returns the nickname of the player
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * returns the connection socket of the player
     */
    public ServerSocketThread getSocket() {
        return thread;
    }

    /**
     * @return the lobby a player is currently in
     */
    public Lobby getLobby() {
        return lobby;
    }

    /**
     * sets the lobby this player is in
     */
    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    /**
     * sets the player ready if he is not,
     * if he is ready makes him unready
     *
     * @param b is the boolean for ready status
     */
    public void setReady(boolean b) {
        this.ready = b;
    }
}
