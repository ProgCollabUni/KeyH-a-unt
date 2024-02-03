package server;

import net.Protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import player.Player;

/**
 * server side thread that communicates with the client using the network protocol
 */
public class ServerSocketThread extends net.SocketThread {

    private int id;

    private ServerTasks task;

    /**
     * player this socket belongs to
     */
    private Player player;

    private static Logger logger = LogManager.getRootLogger();


    /**
     * constructs a new server thread
     */
    public ServerSocketThread(int id, Socket socket) throws IOException {
        super(socket);
        this.task = new ServerTasks(this);
        this.id = id;
        Server.addThread(this);

        // Adds and Creates a new Player corresponding to the ServerThread
        this.player = new Player(this);
        Server.addPlayer(this.player);
        logger.info("created new Server Connection Thread " + id);
    }


    /**
     * starts server thread
     */
    @Override
    public void run() {
        logger.info("Thread " + id + " connected...");
        super.run();
    }

    /**
     * We decode the inputs
     * @throws IOException
     */
    @Override
    public void decode() throws IOException {
        try {
            String read = in.readLine();
            if (read == null) {
                throw new SocketException();
            }
            Protocol cmd = Protocol.getCommand(read);
            task.receive(cmd, read);
        } catch (SocketException | SocketTimeoutException e) {
            logger.debug("disconnecting after Exception:", e);
            disconnect();
        }
    }

    @Override
    public void send(Protocol cmd, String msg) {
        super.send(cmd, msg);
        logger.trace(
            "Sending to player " + player.getNickname() + ": " + cmd.getString() +
                Protocol.DELIMITER + msg);
    }

    /**
     * We disconnect the thread
     */
    @Override
    public void disconnect() {
        task.logout(); //As long as we don't have reconnecting functionality
        super.disconnect();
        Server.removeThread(this);
        logger.debug("Thread " + id + " disconnected...");
    }

    /**
     * returns the thread ID that was assigned on creation
     */

    public int getId() {
        return id;
    }

    /**
     * @return the player of this socket connection
     */
    public Player getPlayer() {
        return player;
    }


}

