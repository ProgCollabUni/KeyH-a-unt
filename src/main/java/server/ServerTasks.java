package server;

import net.Protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import player.Player;
import server.game.utility.GameException;
import server.game.utility.GameActions;

import java.util.Arrays;

/**
 * this class is responsible for all the tasks that are executed by the server
 * <p>
 * commands that are coming in go to the
 * {@link ServerTasks#receive(Protocol, String)} method.
 */
public class ServerTasks {

    private ServerSocketThread parent;

    private static final Logger LOGGER = LogManager.getRootLogger();

    /**
     * attaches a Server task to a thread
     */
    public ServerTasks(ServerSocketThread ct) {
        this.parent = ct;
    }

    /**
     * Here we call the corresponding method via a switch-case statement.
     *
     * @param cmd  NetworkProtocol command
     * @param args Arguments
     */
    public void receive(Protocol cmd, String args) {
        LOGGER.trace(
            "received " + args + " from Player " + parent.getPlayer().getNickname());

        switch (cmd) {
            case BROADCAST ->
                Server.sendToAll(Protocol.BROADCAST, Protocol.getMessage(args));
            case GLOBALCHAT ->
                globalChat(Protocol.GLOBALCHAT, this.parent, Protocol.getMessage(args));
            case CHANGENICKNAME -> changeNickname(Protocol.getMessage(args));
            case PING -> ping(Protocol.getMessage(args));
            case PONG -> pong();
            case CREATELOBBY -> createLobby(Protocol.getMessage(args));
            case JOINLOBBY -> joinLobby(Protocol.getMessage(args));
            case LOBBYCHAT -> lobbyChat(Protocol.getMessage(args));
            case DISPLAYLOBBY -> displaylobby();
            case TOGGLEREADY -> toggleReady();
            case GAMEACTION -> gameAction(args);
            case GAMELIST -> listAllGames();

            case DISCONNECT -> parent.disconnect();
            case LOGOUT -> parent.disconnect();
            case ERROR -> parent.send(Protocol.ERROR, Protocol.getMessage(args));
            case CLOSELOBBY -> closeLobby();
            case LEAVELOBBY -> leaveLobby();
            case PLAYERLIST -> playerlist(Protocol.getMessage(args));
            case WHISPERLIST -> parent.send(Protocol.WHISPERLIST, Server.printPlayers());
            case WHISPER -> whisper(args);
            case HIGHSCORES -> sendHighscores();
            default -> parent.send(Protocol.ERROR,
                " Server error with message: " + args + ": Command not found");
        }
    }


    /**
     * It changes the nickname of client with the player class
     */
    public void changeNickname(String nickname) {
        String tempName = parent.getPlayer().getNickname();
        if (Server.findPlayer(nickname) != null) {
            parent.send(Protocol.DISPLAY, "This Name was taken, improving...");
            nickname = nickname + improveName();
        }
        parent.send(Protocol.DISPLAY,
            "You have successfully changed your nickname into " + nickname);
        parent.getPlayer().setNickname(nickname);
        parent.send(Protocol.CHANGENICKNAME, nickname);
    }

    /**
     * Send message client to Client without other players getting notified
     *
     * @param input is the protocol~~recipient~~message
     */
    public void whisper(String input) {
        String[] par = Protocol.getParameters(input, 1);
        directMessage(Protocol.WHISPER, parent.getPlayer().getNickname() + ": " + par[2],
            par[1]);
    }

    /**
     * Helper method for improving names.
     * Picks a random title/description to a player name.
     */
    public String improveName() {
        return "_" + parent.getId();
    }

    /**
     * this method writes a message to a specified client
     */
    private void directMessage(Protocol cmd, String msg, String recipient) {
        ServerSocketThread toSend = Server.findConnectedThread(recipient);
        if (toSend != null) {
            toSend.send(cmd, msg);
        } else {
            parent.send(Protocol.ERROR, "recipient " + recipient + " doesn't exist!");
            parent.send(Protocol.DISPLAY, "recipient " + recipient + " doesn't exist!");
        }
    }

    /**
     * sends a command to all clients with sender info
     */
    public static void globalChat(Protocol cmd, ServerSocketThread sender, String msg) {
        Server.sendToAll(cmd, sender.getPlayer().getNickname() + ": " + msg);
    }

    /**
     * for demonstration purposes, shows manual pings
     */
    public void ping(String type) {
        if (type.equals("manual")) {
            LOGGER.info("Ping from Player " + parent.getPlayer().getNickname());
        }
        parent.send(Protocol.PONG, type);
    }

    /**
     * resets the number of pings the associated client missed
     */
    public void pong() {
        parent.resetMissedPings();
    }

    /**
     * logs out the client and sends every player a message
     */
    public void logout() {
        String name = parent.getPlayer().getNickname();
        if (parent.getPlayer().getLobby() != null) {
            this.leaveLobby();
        }
        Server.removePlayer(parent.getPlayer());
        //parent.disconnect(); remove when adding reconnect
        Server.sendToAll(Protocol.DISPLAY, name + " has logged out");
        LOGGER.info("Player " + name + " logged out");
    }

    /**
     * creates a new lobby with the player submitting the command
     *
     * @param name name of the lobby
     */
    public void createLobby(String name) {
        if (parent.getPlayer().getLobby() != null) {
            parent.getPlayer().getLobby().leave(parent.getPlayer());
        }
        Lobby l = new Lobby(parent.getPlayer(), name);
        if (Server.findLobby(name) != null) {
            parent.send(Protocol.DISPLAY, "Lobby " + name +
                " already exists, please choose a different name or join that lobby");
        } else {
            Server.addLobby(l);
            parent.send(Protocol.DISPLAY, "Lobby " + name + " created");
        }
    }

    /**
     * submitting player joins a lobby
     *
     * @param name name of the lobby
     */
    public void joinLobby(String name) {
        Lobby l = Server.findLobby(name);
        if (l != null) {
            Player p = parent.getPlayer();
            try {
                l.join(p);
                p.setLobby(l);
                l.sendToAll(Protocol.PLAYERLIST,
                    "Players in lobby" + Protocol.DELIMITER + l.printPlayers());
            } catch (LobbyFullException e) {
                LOGGER.warn("Player " + p.getNickname() + "tried to join a full lobby");
                parent.send(Protocol.ERROR, "This lobby is already full!");
            }
        } else {
            parent.send(Protocol.ERROR, "You were not able to join Lobby " + name);
        }
    }

    /**
     * sends a message to all players in {@link ServerTasks#parent} player's lobby
     */
    public void lobbyChat(String msg) {
        Player p = parent.getPlayer();
        if (p.getLobby() == null) {
            parent.send(Protocol.DISPLAY, "Your are not currently in a lobby");
        } else {
            lobbySend(Protocol.LOBBYCHAT,
                p.getLobby().getName() + ": " + p.getNickname() + ": " + msg, p);
        }
    }

    /**
     * sends a transmission to the lobby of Player p
     */
    public void lobbySend(Protocol cmd, String msg, Player p) {
        Lobby l = p.getLobby();
        if (l == null) {
            p.getSocket().send(Protocol.ERROR, "You are not in a valid Lobby");
        } else {
            l.sendToAll(cmd, msg);
        }
    }

    /**
     * closen lobby l and removes all players from the lobby
     */
    public void closeLobby() {
        Lobby playerLobby = parent.getPlayer().getLobby();
        if (playerLobby == null) {
            parent.send(Protocol.DISPLAY, "You are not currently in a lobby");
        } else {
            playerLobby.closeLobby();
        }
    }

    /**
     * leave the lobby you are in
     */
    public void leaveLobby() {
        Player p = parent.getPlayer();
        Lobby playerLobby = p.getLobby();
        if (playerLobby == null) {
            // Do nothing, as Player is not currently in a lobby
            parent.send(Protocol.DISPLAY, "You are not currently in a lobby");
        } else {
            playerLobby.leave(p);
            parent.send(Protocol.DISPLAY, "You have left lobby " + playerLobby.getName());
            LOGGER.info(
                "Player" + p.getNickname() + " left Lobby " + playerLobby.getName());
        }
    }

    /**
     * toggles the ready status of the player
     */
    public void toggleReady() {
        Player p = parent.getPlayer();
        if (p.getLobby() == null) {
            parent.send(Protocol.DISPLAY, "You are not currently in a lobby");
        } else {
            parent.getPlayer().toggleReady();
        }
    }

    /**
     * Prints the corresponding playerList, either serverlist or lobbylist
     * depending on the arguments... 'server' or 'lobby'.
     *
     * @param list either 'server' or 'list', depending on which list you want
     */
    public void playerlist(String list) {
        if (list.equals("Players in server")) {
            parent.send(Protocol.PLAYERLIST,
                "Players in server" + Protocol.DELIMITER + Server.printPlayers());
        } else if (list.equals("Players in lobby")) {
            if (parent.getPlayer().getLobby() == null) {
                parent.send(Protocol.DISPLAY,
                    "You are not currently" + " connected to a lobby");
            } else {
                parent.send(Protocol.PLAYERLIST, "Players in lobby" + Protocol.DELIMITER +
                    parent.getPlayer().getLobby().printPlayers());
            }
        } else {
            parent.send(Protocol.DISPLAY,
                "This list doesn't exist," + " please specify 'server' or 'lobby'");
        }
    }

    /**
     * displays all the String of all the lobbies
     */
    public void displaylobby() {
        parent.send(Protocol.DISPLAYLOBBY, Server.printAllLobbies());
    }

    /**
     * Sends the action to the game package
     *
     * @param args is the input from Client about what he wants to do in the game
     */
    public void gameAction(String args) {
        String[] parameters = Protocol.getParameters(args, 2);
        Player p = parent.getPlayer();
        try {
            GameActions action = new GameActions(parameters);
            p.getLobby().gameMove(p, action);
        } catch (GameException e) {
            LOGGER.error("unable to decode game action: ", e);
            parent.send(Protocol.ERROR,
                "unable to decode game action: " + Arrays.toString(parameters));
            parent.send(Protocol.REQUESTACTION,
                "You've entered a non-existing move, try again!");
        }

    }

    /**
     * sends a list of all current and past games of open lobbies
     */
    public void listAllGames() {
        String res = "";
        if (Server.getLobbies().size() == 0) {
            parent.send(Protocol.ERROR,
                "Unable to display game-list," + " since there are no lobbies");
        } else {
            for (Lobby l : Server.getLobbies()) {
                res = res + l.listGames() + Protocol.DELIMITER;
            }
            res = res.substring(0, res.length() - 1);
            parent.send(Protocol.GAMELIST, res);
        }
    }

    /**
     * sends the servers high-scores to the requesting player
     */
    public void sendHighscores() {
        parent.send(Protocol.HIGHSCORES, Server.getHighScores());
    }


    /**
     * these methods are used for the tests
     */
    public ServerSocketThread getParent() {
        return this.parent;
    }


}
