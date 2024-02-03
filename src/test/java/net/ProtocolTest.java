package net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ProtocolTest {

    /**
     * Test to see if the command is corectly  returned
     */
    @Test
    void getCommand() {
        //we can assume from these tests passed, that cmd gets extracted correctly
        String msg = "";
        Protocol cmd = Protocol.getCommand(msg);
        assertEquals(Protocol.NOTFOUND, cmd);

        msg = "CHAT~hi";
        cmd = Protocol.getCommand(msg);
        assertEquals(Protocol.GLOBALCHAT, cmd);

        msg = "CHATsjdk~hi";
        cmd = Protocol.getCommand(msg);
        assertEquals(Protocol.NOTFOUND, cmd);

        msg = "Chat~sjdk~hi";
        cmd = Protocol.getCommand(msg);
        assertEquals(Protocol.NOTFOUND, cmd);

        msg = "WHIP~yash~hi";
        cmd = Protocol.getCommand(msg);
        assertEquals(Protocol.WHISPER, cmd);

        msg = "WH~IP~yash~hi";
        cmd = Protocol.getCommand(msg);
        assertEquals(Protocol.NOTFOUND, cmd);
    }

    /**
     * Tests if the getmessage method returns correct output
     */
    @Test
    void testGetMessage() {
        String msg = "CHAT~hello";
        assertEquals("hello", Protocol.getMessage(msg));

        msg = "";
        assertEquals("", Protocol.getMessage(msg));

        msg = "PING~";
        assertEquals("", Protocol.getMessage(msg));

    }

    /**
     * Tests if we get the correct arguments
     */
    @Test
    void testGetArguments() {
        String msg = "WHIP~yash~hello there!";
        String[] params = Protocol.getParameters(msg, 1);
        assertEquals("WHIP", params[0]);
        assertEquals("yash", params[1]);
        assertEquals("hello there!", params[2]);


        msg = "PLST~yash~luke~sevi~michel~";
        params = Protocol.getParameters(msg, 1);
        assertEquals("PLST", params[0]);
        assertEquals("yash", params[1]);
        assertEquals("luke~sevi~michel~", params[2]);
        params = Protocol.getParameters(msg, 5);
        assertEquals("yash", params[1]);
        assertEquals("luke", params[2]);
        assertEquals("sevi", params[3]);
        assertEquals("michel", params[4]);
        assertEquals("", params[5]);

    }

}
