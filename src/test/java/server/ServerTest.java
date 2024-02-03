package server;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import player.Player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * We test the server  functionalities
 */
class ServerTest {


    /**
     * This is a testing method for adding and finding a player
     */
    @Test
    void testAddAndFindPlayer() {
        // create a mock player object
        Player mockPlayer = Mockito.mock(Player.class);
        when(mockPlayer.getNickname()).thenReturn("testPlayer");

        // add the mock player to the player list
        Server.addPlayer(mockPlayer);

        // call the findPlayer method with the player's nickname
        Player foundPlayer = Server.findPlayer("testPlayer");

        // verify that the correct player was found
        assertEquals(mockPlayer, foundPlayer);
        assertEquals(null, Server.findPlayer("other"));
    }

    /**
     * This is a test method for adding and finding a lobby
     */
    @Test
    void testAddAndFindLobby() {
        // create a mock lobby object with the mock game
        Lobby mockLobby = Mockito.mock(Lobby.class);
        when(mockLobby.getName()).thenReturn("testLobby");

        // add the mock lobby to the lobby list
        Server.addLobby(mockLobby);

        // call the findLobby method with the lobby's name
        Lobby foundLobby = Server.findLobby("testLobby");

        // verify that the correct lobby was found
        assertEquals(mockLobby, foundLobby);
    }

}
