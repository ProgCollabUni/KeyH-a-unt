package net;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import server.Server;
import server.ServerSocketThread;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * this class theoretically tests both {@link server.ServerSocketThread}
 * and {@link client.ClientSocketThread} threads since they
 * are symmetric besides the {@link SocketThread#decode()}
 * method which calls different tasks.
 */
public class NetworkSocketTest {

    PipedInputStream inputStream = new PipedInputStream();
    BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(inputStream));

    PipedOutputStream outputStream = new PipedOutputStream();


    int id = 0;
    Socket sock = mock(Socket.class);

    ServerSocket serverSocket = mock(ServerSocket.class);


    ServerSocketThread socketThread;

    /**
     * We create a setup
     * @throws IOException
     */
    @BeforeEach
    void setUp() throws IOException {
        when(serverSocket.accept()).thenReturn(sock);
        inputStream.connect(outputStream);
        when(sock.getInputStream()).thenReturn(inputStream);
        when(sock.getOutputStream()).thenReturn(outputStream);
        doCallRealMethod().when(sock).setSoTimeout(anyInt());
        try {
            socketThread = new ServerSocketThread(id, sock);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * We test the decode method
     * @throws IOException
     */
    @Test
    void testDecode() throws IOException {
        String message = Protocol.BROADCAST.getString() + Protocol.DELIMITER + "hi";
        outputStream.write((message + System.lineSeparator()).getBytes());
        socketThread.decode();
        String in = bufferedReader.readLine();
        assertEquals(message, in);
    }

    /**
     * We test if wrong decode works
     * @throws IOException
     */
    @Test
    void testWrongDecode() throws IOException {
        String message = "BSCT~hi";
        outputStream.write((message + System.lineSeparator()).getBytes());
        socketThread.decode();
        String in = bufferedReader.readLine();
        assertEquals("ERRR~ Server error with message: BSCT~hi: Command not found", in);
    }

    /**
     * We test the chat method
     * @throws IOException
     */
    @Test
    void testChat() throws IOException {
        String message = Protocol.GLOBALCHAT.getString() + Protocol.DELIMITER + "hello";
        outputStream.write((message + System.lineSeparator()).getBytes());
        socketThread.decode();
        String in = bufferedReader.readLine();
        String nick = socketThread.getPlayer().getNickname();
        String expected =
            Protocol.GLOBALCHAT.getString() + Protocol.DELIMITER + nick + ": hello";
        assertEquals(expected, in);
    }

    /**
     * We test the send method
     * @throws IOException
     */
    @Test
    void testSend() throws IOException {
        socketThread.send(Protocol.LOBBYCHAT, "hello!");
        String out = bufferedReader.readLine();
        String expected = Protocol.LOBBYCHAT.getString() + Protocol.DELIMITER + "hello!";
        assertEquals(expected, out);
    }

    /**
     * We test the disconnect method
     */
    @Test
    void testDisconnect() {
        socketThread.getPlayer().setNickname("toFind");
        assertEquals(socketThread,
            Server.findConnectedThread(socketThread.getPlayer().getNickname()));
        socketThread.disconnect();
        assertNull(Server.findConnectedThread(socketThread.getPlayer().getNickname()));
    }

}
