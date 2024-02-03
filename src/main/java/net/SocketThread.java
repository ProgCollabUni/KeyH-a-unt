package net;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

/**
 * this is an abstract class of a Socketthread which both client
 * and server can use as their implementation is mostly
 * symmetrical
 */
public abstract class SocketThread implements Runnable {
    protected Socket socket;
    protected BufferedWriter out;
    protected BufferedReader in;
    private volatile boolean active;
    private int missedPings = 0;

    private static Logger logger = LogManager.getRootLogger();

    /**
     * constructs a basic network communication thread
     *
     * @param socket the socket to be communicated through
     */
    public SocketThread(Socket socket) throws IOException {
        this.socket = socket;
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * starts a networking thread
     */
    public void run() {
        active = true;
        // Start Ping Service
        Thread thread = new Thread(new PingService(this));
        thread.start();

        try {
            socket.setSoTimeout(3 * Protocol.PINGINTERVAL);
            while (active) {
                // Reads from the incoming network
                decode();
            }
        } catch (IOException e) {
            logger.error("Exception in Socket:", e);
        } finally {
            if (active) {
                disconnect();
            }
        }
    }

    /**
     * decoding method must be implemented separately on client/server side
     * due to commands being handled differently on either side
     */
    public abstract void decode() throws IOException;

    /**
     * sends a transmission through the network
     *
     * @param command The command to send
     * @param data    The String containing the data
     */
    public void send(Protocol command, String data) {
        try {
            out.write(command.getString() + Protocol.DELIMITER);
            out.write(data);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            logger.debug("exception while sending ", e);
        }
    }

    /**
     * handles a disconnecting socket
     */
    public void disconnect() {
        active = false;
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Exception while closing Socket", e);
        }
    }

    /**
     * @return whether this thread is still active
     */
    public boolean getActive() {
        return this.active;
    }

    /**
     * set the activity of this thread e.g. when timing out
     */
    public void setActive(boolean b) {
        this.active = b;
    }

    /**
     * reset the number of pings this socket missed
     */
    public void resetMissedPings() {
        this.missedPings = 0;
    }

    /**
     * increments the number of missed pings of this socket
     */
    public void incrementMissedPings() {
        this.missedPings++;
    }

    /**
     * returns the number of currently missed pings
     */
    public int getMissedPings() {
        return missedPings;
    }
}
