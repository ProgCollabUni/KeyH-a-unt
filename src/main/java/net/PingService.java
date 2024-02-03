package net;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * this class is a abstract class used by server and client for the ping service
 */
public class PingService implements Runnable {

    private SocketThread parent;

    private static Logger logger = LogManager.getRootLogger();

    /**
     * creates a ping service
     * @param th network thread this service sends through
     */
    public PingService(SocketThread th) {
        this.parent = th;
    }

    /**
     * the run method for ping
     */
    @Override
    public void run() {
        try {
            Thread.sleep(Protocol.PINGINTERVAL);
            while (parent.getActive()) {

                if (parent.getMissedPings() > 3) {
                    parent.setActive(false);
                } else {
                    parent.send(Protocol.PING, "");
                    parent.incrementMissedPings();
                }
                Thread.sleep(Protocol.PINGINTERVAL);

            }

        } catch (InterruptedException e) {
            logger.debug("Exception thrown: ", e);
            throw new RuntimeException(e);
        }
    }
}
