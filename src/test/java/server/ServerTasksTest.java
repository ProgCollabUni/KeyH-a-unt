package server;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import player.Player;

/**
 * We test the ServerTask class
 */
public class ServerTasksTest {
    //Initiali
    ServerSocketThread ssT = mock(ServerSocketThread.class);
    ServerTasks sT = new ServerTasks(ssT);

    /**
     * Before starting the testing we need to make the setup
     */
    @BeforeEach
    void setUp() {
        Player p = new Player(ssT);
        when(ssT.getPlayer()).thenReturn(p);
    }


    /**
     * checks if Nickname is changed correctly
     */
    @Test
    void changeNickname() {
        String newName = "Michel";
        sT.changeNickname("Michel");
        assertEquals(newName, sT.getParent().getPlayer().getNickname());
    }

    /**
     * Checks if Ping resets
     */
    @Test
    void testPingReset() {
        doCallRealMethod().when(ssT).incrementMissedPings();
        doCallRealMethod().when(ssT).getMissedPings();
        doCallRealMethod().when(ssT).resetMissedPings();
        ssT.incrementMissedPings();
        ssT.incrementMissedPings();
        assertEquals(2, ssT.getMissedPings());
        sT.pong();
        assertEquals(0, ssT.getMissedPings());
    }
}
