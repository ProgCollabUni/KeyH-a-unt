

import net.LoggingHandler;

import client.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.Server;

public class Main {

    /**
     * main Method, decides to launch server or client
     */
    public static void main(String[] args) {

        //Hard code logging for now
        LoggingHandler.enableLogging(true);
        Logger logger = LogManager.getRootLogger();

        if (args.length >= 2) {
            if (args[0].equals("server")) {
                Server.main(args[1]);
            } else if (args[0].equals("client") && args.length > 1 && args.length < 4) {
                String username = System.getProperty("user.name").replaceAll(" ", "_");
                if (args.length == 3) {
                    username = args[2];
                }
                Client.main(args[1], username);
            } else {
                logger.fatal("unrecognized Arguments, please specify " +
                    "client <hostadress>:<port> [<username>] | server <port>");
            }

        }
    }
}
