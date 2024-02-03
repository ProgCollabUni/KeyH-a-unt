package client;

import client.gui.GameController;
import net.Protocol;

import java.util.Timer;
import java.util.TimerTask;

/**
 * starts a timer when its clients turn
 */
public class GameTimer {
    private Timer timer = new Timer();
    private volatile boolean isRunning = false;

    private ClientSocketThread socket;

    private int duration = 20;

    private GameController gameController;

    /**
     * creates a timer for the client socket th
     */
    public GameTimer(ClientSocketThread th, GameController controller) {
        this.socket = th;
        this.gameController = controller;
    }

    /**
     * starts the timer for a specific period
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    onTimerExecution();
                }
            }, 0, 1000);
        }
    }

    /**
     * to stop the timer
     */
    public void stop() {
        if (isRunning) {
            duration = 0;
            isRunning = false;
            timer.cancel();
            timer = new Timer();
        }
    }

    /**
     * executed when timer is complete
     */
    protected void onTimerExecution() {
        gameController.setTimer(duration);
        if (duration == 0) {
            onTimerCompletion();
        }
        duration--;
    }

    /**
     * when the timer is finished sends the socket the message that his turn is skipped
     */
    protected void onTimerCompletion() {
        socket.send(Protocol.GAMEACTION, "SKIP");
        stop();
    }

    /**
     * @return the status of the timer
     */
    protected boolean isRunning() {
        return isRunning;
    }

    /**
     * resets the clock
     */
    public void reset() {
        stop();
        duration = 20;
    }
}
