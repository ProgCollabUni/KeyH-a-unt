package client;

// We need to call the protocol according to the input

import client.gui.ChatApp;
import client.gui.GUI;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * A Client gets created in this class with his user Interface
 */
public class Client {

    private static Logger logger = LogManager.getRootLogger();


    /**
     * launches a new client instance
     */
    public static void main(String host, String username) {

        try {

            GUI gui = new GUI(new ChatApp(), host, username);
            Thread guiThread = new Thread(gui);
            guiThread.start();

        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
    }
}
