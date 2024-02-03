package server;

import net.Protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import player.Player;
import server.game.Game;
import server.game.utility.GameActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

enum LobbyState {
    JOINING(), WAITING(), INGAME()

}

/**
 * Lobby class to manage game instances
 */
public class Lobby {

    /**
     * maximum amount of players allowed per lobby
     */
    public static final int MAXPLAYERS = 4;
    private static Logger logger = LogManager.getRootLogger();
    private List<Player> players;
    private String name;
    private boolean allReady = false;
    private List<Game> gameList = new ArrayList<>();
    private LobbyState state = LobbyState.JOINING;

    /**
     * creates a new lobby with name n and the creator in it
     */
    public Lobby(Player creator, String n) {
        players = new ArrayList<>();
        players.add(creator);
        creator.setLobby(this);
        name = n;
        logger.info("Created new lobby " + name);
        gameList.add(new Game(name + "_" + gameList.size()));
    }

    /**
     * join this lobby
     */
    public void join(Player p) throws LobbyFullException {
        if (players.size() == MAXPLAYERS) {
            throw new LobbyFullException();
        } else {
            if (p.getLobby() == this) {
                return;
            } else if (p.getLobby() != null) {
                p.getLobby().leave(p);
            }
            players.add(p);
            p.setLobby(this);
            logger.trace("Player " + p.getNickname() + " joined lobby " + name);
            if (players.size() == MAXPLAYERS) {
                state = LobbyState.WAITING;
            }
        }
    }

    /**
     * leave this lobby, closes lobby when nobody's left
     */
    public void leave(Player p) {
        Game currentGame = gameList.get(gameList.size() - 1);
        if (currentGame.isRunning()) {
            currentGame.processLogout();
            this.state = LobbyState.WAITING;
            sendToAll(Protocol.ENDGAME, "");
            sendToAll(Protocol.DISPLAY, "Stopped game because somebody left!");
            gameList.add(new Game(name + "_" + gameList.size()));
            logger.warn("Stopped game in lobby " + this.name +
                " because a player logged out!");
        }
        players.remove(p);
        p.setLobby(null);
        this.sendToAll(Protocol.DISPLAY,
            "Player " + p.getNickname() + " has left the lobby");
        logger.info(
            "Player" + p.getNickname() + " left Lobby " + this.getName());
        state = LobbyState.JOINING;
        if (players.size() == 0) {
            logger.info(
                "Closing Lobby " + this.getName() + " since it was empty");
            this.closeLobby();
        }
    }

    /**
     * @return the name of this lobby
     */
    public String getName() {
        return name;
    }

    /**
     * sends a transmission to all players in this lobby
     */
    public void sendToAll(Protocol cmd, String msg) {
        for (Player p : players) {
            p.getSocket().send(cmd, msg);
        }
    }

    /**
     * removes players from the Lobby and closes the Lobby
     */
    public void closeLobby() {
        logger.info("Shutting down lobby " + name);
        this.sendToAll(Protocol.DISPLAY, "Lobby closing ...");
        for (Player p : players) {
            p.setLobby(null);
        }
        Server.getLobbies().remove(this);
    }

    /**
     * @return players list of the lobby
     */
    public List<Player> getPlayerLobbyList() {
        return this.players;
    }

    /**
     * * @return true if all players are ready and the lobby is full
     */
    public void lobbyReadyCheck() {
        boolean b = true;
        for (Player p : players) {
            b = b && p.isReady();
        }
        allReady = b;
        if (b && state == LobbyState.WAITING) {
            startGame();
        }
    }

    /**
     * starts the game once the last player has readied up
     */
    public void startGame() {
        if (this.allReady && !gameList.get(gameList.size() - 1).isRunning()) {
            sendToAll(Protocol.STARTGAME, "");
            gameList.get(gameList.size() - 1).start(this);
            logger.info("starting game in lobby " + name);
            state = LobbyState.INGAME;
        }
    }

    /**
     * shuffles the player list to match the spawn positions assigned
     */
    public void shufflePlayerList() {
        Collections.shuffle(players);
        logger.debug("shuffled players in lobby " + name);

    }

    /**
     * submits a move to this lobby's game
     *
     * @param p      Player submitting the move
     * @param action encoded game action
     */
    public void gameMove(Player p, GameActions action) {
        if (gameList.get(gameList.size() - 1).isRunning()) {
            gameList.get(gameList.size() - 1)
                .playerMove(players.indexOf(p), action);
        } else {
            p.getSocket().send(Protocol.DISPLAY,
                "You cannot make a move now, the game is not running");
        }
    }

    /**
     * gets triggered to finish the game, unreadies everybody
     */
    public void finishGame() {
        sendToAll(Protocol.DISPLAY,
            "Game is over! " +
                gameList.get(gameList.size() - 1).printWinner());
        sendToAll(Protocol.ENDGAME, "");
        for (Player p : players) {
            p.toggleReady();
        }
        state = LobbyState.WAITING;
        logger.info("Game over in Lobby " + getName() + ", won by " +
            gameList.get(gameList.size() - 1).getWinner());
        gameList.add(new Game(name + "_" + gameList.size()));
    }


    /**
     * Prints all players currently connected to the lobby
     *
     * @return String with all player-names in brackets [].
     */
    public String printPlayers() {
        String playerlist = "";
        for (Player p : players) {
            String readyStatus = "Not Ready";
            if (p.isReady()) {
                readyStatus = "READY";
            }
            playerlist =
                playerlist + "[" + p.getNickname() + "] " + readyStatus +
                    Protocol.DELIMITER;
        }
        playerlist = playerlist.trim();
        playerlist = playerlist.substring(0, playerlist.length() - 1);
        return playerlist;
    }

    /**
     * @return a String with all the past and current games of this lobby
     */
    public String listGames() {
        String out = "Games in Lobby " + name + ": ";
        for (Game g : gameList) {
            String status = g.getState();
            out = out + "[" + status + " " + g.getWinner() + "] ";
        }
        out = out.trim();
        return out;
    }
}

/**
 * this exception is thrown if a player tries to join a full lobby
 */
class LobbyFullException extends Exception {
    public LobbyFullException() {
        super("Cannot join, Lobby already has 4 players!");
    }
}
