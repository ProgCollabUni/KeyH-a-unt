package client;

import net.Protocol;
import net.SocketThread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import client.gui.GUI;

/**
 * client side thread that communicates with the server using the network protocol
 */
public class ClientSocketThread extends SocketThread {

    private ClientTasks clientTasks;
    private String username;

    private static Logger logger = LogManager.getRootLogger();

    /**
     * start a client-side connection with a username
     */
    public ClientSocketThread(Socket socket, String username) throws IOException {
        super(socket);
        this.username = username;
        clientTasks = new ClientTasks(this);
    }

    /**
     * starts the client side socket
     */
    public void run() {
        send(Protocol.CHANGENICKNAME, username);
        super.run();
        logger.debug("started new Client Connection thread " + username);
    }

    @Override
    public void decode() throws IOException {
        try {
            String input = in.readLine();
            if (input == null) {
                throw new SocketException();
            }
            Protocol receivedCommand = Protocol.getCommand(input);
            clientTasks.receive(receivedCommand, input);

        } catch (SocketException | SocketTimeoutException e) {
            disconnect();
            logger.debug("disconnecting after: ", e);
        }

    }

    /**
     * disconnects the client from server
     */
    @Override
    public void disconnect() {
        GUI.getApplication().showWelcomeScene();
        super.disconnect();
        logger.info("Connection to server closed...");
    }

    /**
     * returns client task instance
     */
    public ClientTasks getClientTasks() {
        return clientTasks;
    }

}
