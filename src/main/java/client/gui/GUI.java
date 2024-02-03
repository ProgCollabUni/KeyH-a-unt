package client.gui;


import client.ClientSocketThread;
import javafx.application.Application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

/**
 * Class start Application and pipes the socket thread through.
 */
public class GUI implements Runnable {
    /**
     * declare chatApp
     */
    public static ChatApp chatApp;

    static ClientSocketThread socketThread;
    private String address;
    private String name;

    /**
     * logger
     */
    private static Logger logger = LogManager.getRootLogger();

    /**
     * Link chatApp and socket thread to gui.
     *
     * @param app is the base GUI.
     */
    public GUI(ChatApp app, String address, String username) {
        chatApp = app;
        this.name = username;
        this.address = address;
    }

    /**
     * gets the socket thread
     */
    public static ClientSocketThread getSocketThread() {
        return socketThread;
    }

    /**
     * launches a connection to a server at a address with a username
     */
    public static void launchClientSocket(String address, String username)
        throws IOException {
        String[] addr = address.split(":");
        Socket socket = new Socket(addr[0], Integer.parseInt(addr[1]));
        socketThread = new ClientSocketThread(socket, username);
        Thread thread = new Thread(socketThread);
        thread.start();
    }

    /**
     * Start GUI
     */
    @Override
    public void run() {

        Application.launch(chatApp.getClass(), address, name);
        logger.info("Starting GUI");
    }

    /**
     * @return the Application associated with this GUI thread
     */
    public static ChatApp getApplication() {
        return chatApp;
    }
}
