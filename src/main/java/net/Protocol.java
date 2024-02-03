package net;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * this is the network protocol of how server and client communicate with each other
 * every command is explained
 */
public enum Protocol {

    /**
     * Client to Server: The client sends a message to the server for the
     * global chat; gets displayed in the chatBox
     */
    GLOBALCHAT("CHAT"),

    /**
     * Server sends message to every player/client, gets displayed in the
     * chatBox
     */
    BROADCAST("BCST"),

    /**
     * Server sends error to client
     * Gets displayed into Error-AlertBox
     */
    ERROR("ERRR"),

    /**
     * gets returned in case no command matches
     */
    NOTFOUND(""),

    /**
     * command send by the client to change his nickname,
     * which (nickname) is displayed for example in the chat
     * Gets displayed in the label above the chat and in front of
     * every chat message sent
     */
    CHANGENICKNAME("CNGN"),

    /**
     * sends a ping message
     * Can be requested by the client and then measures the network ping
     */
    PING("PING"),

    /**
     * Used to send a Pong back to the client
     */
    PONG("PONG"),

    /**
     * is used to force disconnects, effectively closing the socket
     */
    DISCONNECT("DSCN"),

    /**
     * Client can logout and disconnect himself
     */
    LOGOUT("LOGO"),

    /**
     * If the client receives this command, the message gets
     * displayed in the Info-Box.
     */
    DISPLAY("DSPL"),

    /**
     * create a new lobby
     * usage: select the corresponding menu and type your desired
     * name in the AlertBox
     */
    CREATELOBBY("CLOB"),

    /**
     * join a existing lobby via the menu and click it to join
     */
    JOINLOBBY("JLOB"),

    /**
     * send a chat to the lobby you are in
     * gets displayed into the ChatBox
     */
    LOBBYCHAT("LCHT"),

    /**
     * closes the lobby and removes all players from the lobby
     */
    CLOSELOBBY("QLOB"),

    /**
     * client leaves the lobby
     */
    LEAVELOBBY("LLOB"),

    /**
     * used to make a move in the game
     */
    GAMEACTION("GMAC"),

    /**
     * toggles the player's "ready" status
     */
    TOGGLEREADY("TRDY"),

    /**
     * requests a player's turn.
     */
    REQUESTACTION("REQA"),

    /**
     * used to transmit room info
     */
    ROOMINFO("RINF"),

    /**
     * Used for Client to Client Whisper-Chat
     */
    WHISPER("WHIP"),

    /**
     * used, if the client wants to get a list of all connected players
     * in his lobby, or in the server as a whole; takes "server" or "lobby"
     * as an argument to differentiate the two cases.
     */
    PLAYERLIST("PLST"),

    /**
     * lists all ongoing/finished games of all open lobbies
     */
    GAMELIST("GLST"),

    /**
     * The client sends this message if it wants to receive a list
     * of possible lobbies to join from the server
     * This list gets printed into the lobbyList in the lounge
     */
    DISPLAYLOBBY("DLOB"),

    /**
     * used to send information while the game is running;
     * gets printed into gameInfo Label
     */
    GAMEINFO("GINF"),

    /**
     * used to send player information to the client, f.ex. role ghost
     */
    PLAYERINFO("PINF"),

    /**
     * used to request/communicate the server's highscores
     */
    HIGHSCORES("HSCR"),

    /**
     * starts the game, gets sent from server to client
     */
    STARTGAME("STGM"),

    /**
     * ends the game; used for client GUI
     */
    ENDGAME("ENDG"),

    /**
     * shows that another player is in the same room as you
     */
    ROOMPLAYER("RPLR"),

    /**
     * Used for sending suggestions for names to the client,
     * for the whisper method
     */
    WHISPERLIST("WLST"),

    /**
     * used to send directions towards exit or next player to haunt
     */
    TARGETDIRECTION("TDIR");


    private final String protocolcommand;

    /**
     * Custom delimiter that is used in our network communication.
     * used to separate commands from arguments and messages.
     */
    public static final String DELIMITER = "~";

    /**
     * interval in which the client and server ping each other (in milliseconds)
     */
    public static final int PINGINTERVAL = 20000;

    private static Logger logger = LogManager.getRootLogger();

    Protocol(String protocolcommand) {
        this.protocolcommand = protocolcommand;
    }

    /**
     * @return value of a command
     */
    public String getString() {
        return protocolcommand;
    }

    /**
     * returns the element matching to a given string
     *
     * @param msg message to decode
     * @return a Networkprotocol command corresponding to string
     */
    public static Protocol getCommand(String msg) {
        if (msg.length() >= 4) {
            String[] arr = msg.split(Protocol.DELIMITER, 2);
            String command = arr[0];
            for (Protocol n : Protocol.values()) {
                if (command.equals(n.protocolcommand)) {
                    return n;
                }
            }
        } else {
            logger.warn("getCommand message is too short: " + msg);
        }
        return NOTFOUND;
    }

    /**
     * returns the message of a transmission
     */
    public static String getMessage(String in) {
        String[] arr = in.split(Protocol.DELIMITER, 2);
        if (arr.length == 1) {
            return "";
        } else {
            return arr[1];
        }
    }

    /**
     * splits a transmission into count + 2 pieces
     * the command, count parameters and the remainder
     * note that: nickname cannot have a "~“ as this would
     * result in wrong splitting (important for whisper)
     */
    public static String[] getParameters(String in, int count) {
        return in.split(Protocol.DELIMITER, count + 2);
    }

}
