package server;

import net.Protocol;

import player.Player;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.game.Game;

/**
 * starts the Server on which the clients can connect to
 */
public class Server {

    /**
     * list of all connected threads
     */
    private static List<ServerSocketThread> connectedThreads = new ArrayList<>();

    /**
     * list of all players
     */
    private static List<Player> players = new ArrayList<>();

    /**
     * List of all lobbies (game instances)
     */
    private static List<Lobby> lobbies = new ArrayList<>();

    private static Logger logger = LogManager.getRootLogger();


    private static List<String> highScores;

    private static final String HISCOREPATH =
        System.getProperty("user.dir") + "/khnt.hiscore";

    /**
     * Starts a server instance
     */
    public static void main(String listenport) {
        int status = 0;
        initHighScores();
        int cnt = 0;
        try {
            logger.info("Warte auf Verbindungen auf Port " + listenport + "...");
            ServerSocket echod = new ServerSocket(Integer.parseInt(listenport));

            Thread inThread = new Thread(() -> {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    try {
                        String msg = in.readLine();
                        if (msg.equals("shutdown")) {
                            echod.close();
                            logger.warn("shutting down server from console!");
                            return;
                        }
                        Server.sendToAll(Protocol.BROADCAST, msg);
                    } catch (IOException e) {
                        logger.error("Exception reading in: ", e);
                    }
                }
            });

            inThread.start();

            while (true) {
                Socket socket = echod.accept();
                ServerSocketThread eC = new ServerSocketThread(++cnt, socket);
                Thread eCT = new Thread(eC);
                eCT.start();
            }
        } catch (SocketException e) {
            logger.warn("server shutting down: ", e);
            status = 1;
        } catch (IOException e) {
            logger.error("Exception: ", e);
            status = -1;
        } finally {
            writeHighScores();

        }
        System.exit(status);

    }

    /**
     * appends a server thread to the connected list
     */
    public static void addThread(ServerSocketThread t) {
        connectedThreads.add(t);
    }

    /**
     * removes a server thread from the connected list
     */
    public static void removeThread(ServerSocketThread t) {
        connectedThreads.remove(t);
    }


    /**
     * sends a transmission to all clients
     */
    public static void sendToAll(Protocol cmd, String msg) {
        for (ServerSocketThread ct : connectedThreads) {
            ct.send(cmd, msg);
        }
    }

    /**
     * find the connection associated with a player
     * returns null if the connection is not found
     */
    public static ServerSocketThread findConnectedThread(String name) {
        Player p = findPlayer(name);
        if (p != null) {
            return p.getSocket();
        }
        return null;
    }

    /**
     * adds a player to the global player list
     */
    public static void addPlayer(Player p) {
        players.add(p);
    }

    /**
     * find the player associated to a name
     * returns null if not found
     */
    public static Player findPlayer(String name) {
        for (Player p : players) {
            if (p.getNickname().equals(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * adds a new lobby to the list
     */
    public static void addLobby(Lobby l) {
        lobbies.add(l);
    }

    /**
     * finds a Lobby by name
     * returns null if not found
     */
    public static Lobby findLobby(String name) {
        for (Lobby l : lobbies) {
            if (l.getName().equals(name)) {
                return l;
            }
        }
        return null;
    }

    /**
     * methode returns list of lobbies as a String
     *
     * @return list of lobbies available
     */
    public static String printAllLobbies() {
        if (lobbies.isEmpty()) {
            return "";
        } else {
            String lobbylist = "";
            for (Lobby l : lobbies) {
                lobbylist = lobbylist + l.getName() + Protocol.DELIMITER;
            }
            lobbylist = lobbylist.trim();
            lobbylist = lobbylist.substring(0, lobbylist.length() - 1);
            return lobbylist;
        }
    }

    /**
     * Prints a list of all players currently connected to the server
     *
     * @return String of all Players in brackets[].
     */
    public static String printPlayers() {
        String playerlist = "";
        for (Player p : players) {
            playerlist = playerlist + "[" + p.getNickname() + "]" +
                    Protocol.DELIMITER;
        }
        playerlist = playerlist.trim();
        playerlist = playerlist.substring(0, playerlist.length() - 1);
        return playerlist;
    }

    /**
     * you get the list of all lobbies
     *
     * @return list of all lobbies
     */
    public static List<Lobby> getLobbies() {
        return lobbies;
    }

    /**
     * removes a player of the global player list
     */
    public static void removePlayer(Player player) {
        players.remove(player);
    }

    /**
     * initialize the Highscores, reading or creating the file
     */
    public static void initHighScores() {

        try {
            highScores = new ArrayList<>();
            File highScoreFile = new File(HISCOREPATH);
            boolean created = highScoreFile.createNewFile();
            Scanner scan = new Scanner(highScoreFile);
            if (!created) {
                int i = 0;
                while (scan.hasNext() && i < 3) {
                    highScores.add(scan.nextLine());
                    i++;
                }
                scan.close();
            }

        } catch (IOException e) {
            logger.error("Error initializing Highscores: ", e);
        }
    }

    /**
     * attempts to insert a highscore in the list.
     * replaces or adds a value if it's a better score than any existing ones.
     */
    public static void insertHighScore(Game game) {
        String[] winner = game.getWinner().split(" ");
        int score = Integer.parseInt(winner[0]);
        if (highScores.size() < 3) {
            for (int i = 0; i < highScores.size(); i++) {
                if (score < Integer.parseInt(highScores.get(i).split(" ")[0])) {
                    highScores.add(i, game.getWinner());
                    return;
                }
            }
            highScores.add(game.getWinner());
        } else {
            for (int i = 0; i < 3; i++) {
                if (score < Integer.parseInt(highScores.get(i).split(" ")[0])) {
                    highScores.add(i, game.getWinner());
                    highScores.remove(3);
                    return;
                }
            }
        }
        writeHighScores();

    }

    /**
     * @return the current loaded list of highscores
     */
    public static String getHighScores() {
        String out = "";
        for (String score : highScores) {
            out = out + score + Protocol.DELIMITER;
        }
        return out;
    }

    /**
     * writes the highscores back to the file
     */
    public static void writeHighScores() {
        File highScore = new File(HISCOREPATH);
        try {
            FileWriter writer = new FileWriter(highScore, false);
            writer.write("");
            String out = getHighScores();
            writer.write(out.replaceAll(Protocol.DELIMITER, System.lineSeparator()));
            writer.close();
        } catch (IOException e) {
            logger.error("Error while writing highscores: ", e);
        }
    }
}
