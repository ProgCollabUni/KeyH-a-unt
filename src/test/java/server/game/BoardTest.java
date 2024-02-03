package server.game;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import net.Protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import player.Player;
import server.ServerSocketThread;
import server.game.utility.*;
import server.game.utility.GameActions.Directions;

/**
 * We test the Board class (important for Game Logic tests)
 */
public class BoardTest {


    Board board;

    static Grid grid = new Grid5x5();

    /**
     * we set up everything we need for testing
     */
    @BeforeEach
    void setUp() {
        String gameName = "Test Game";
        Board real = new Board(grid, gameName);
        board = spy(real);
        doNothing().when(board)
            .sendToPosition(anyInt(), any(Protocol.class), anyString());
        doNothing().when(board).sendToAll(any(Protocol.class), anyString());
        Player mock = mock(Player.class);
        when(mock.getNickname()).thenReturn("mockPlayer");
        when(mock.getSocket()).thenReturn(mock(ServerSocketThread.class));
        for (int i = 0; i < 4; i++) {
            board.setPlayerToPosition(i, mock);
        }

    }

    /**
     * Tests if there has been an illegal move
     */
    @Test
    void testIllegalMove() {
        doReturn(false).when(board).runHunterGhostCollisionCheck();
        board.setRole(0, new Hunter());
        assertThrows(GameException.class, () -> board.movePlayer(0, Directions.NORTH));
    }

    /**
     * Tests if move is legal
     */
    @Test
    void testLegalMove() {
        doReturn(false).when(board).runHunterGhostCollisionCheck();
        assertDoesNotThrow(() -> board.movePlayer(0, Directions.SOUTH));
        assertEquals(1, board.getPlayerPositions()[0].getRow());
    }

    /**
     * Tests if Ghosts wraps correctly
     */
    @Test
    void testGhostWrap() {
        doReturn(false).when(board).runHunterGhostCollisionCheck();
        board.setRole(0, new Ghost());
        assertDoesNotThrow(() -> board.movePlayer(0, Directions.NORTH));
        assertEquals(grid.getSize() - 1, board.getPlayerPositions()[0].getRow());

    }

    /**
     * Tests if player collision gives the desired result
     */
    @Test
    void playerCollisionTest() {
        Position winner = board.getPlayerPositions()[1];
        board.setRole(1, new Ghost());
        board.getPlayerPositions()[1].setPosition(0, 0);
        board.getPlayerPositions()[2].setAlive(false);
        assertFalse(board.runHunterGhostCollisionCheck());
        board.getPlayerPositions()[3].setAlive(false);
        assertTrue(board.runHunterGhostCollisionCheck());
        try {
            assertEquals(winner, board.getWinnerPlayer());
        } catch (GameException e) {
            fail();
        }

    }

    /**
     * Tests if hunter can escape
     */
    @Test
    void escapeTest() {
        Position winner = board.getPlayerPositions()[0];
        board.getPlayerPositions()[0].setPosition(grid.getSize() / 2, grid.getSize() / 2);
        assertFalse(board.runEscapeCheck(board.getPlayerPositions()[0]));
        board.getPlayerPositions()[0].increaseLoot();
        assertTrue(board.runEscapeCheck(board.getPlayerPositions()[0]));
        try {
            assertEquals(winner, board.getWinnerPlayer());
        } catch (GameException e) {
            fail();
        }
    }

    /**
     * Tests if room action works
     */
    @Test
    void testRoomAction() {
        board.setRole(1, new Ghost());
        assertThrows(GameException.class, () -> board.roomAction(1));
        board.setRole(1, new Hunter());
        assertFalse(board.getRoom(1).hasChest());
        assertThrows(GameException.class, () -> board.roomAction(1));
        board.getPlayerPositions()[1].setPosition(2, 1);
        assertTrue(board.getRoom(1).hasUnlootedChest());
        assertDoesNotThrow(() -> board.roomAction(1));
        assertTrue(board.getRoom(1).hasChest());
        assertThrows(GameException.class, () -> board.roomAction(1));
        assertEquals(1, board.getPlayerPositions()[1].getLootCount());

    }

    /**
     * Tests if always the nearest player is found
     */
    @Test
    void testFindNearestPlayer() {
        board.setRole(0, new Ghost());
        board.getPlayerPositions()[1].setPosition(2, 2);
        board.findNearestPlayer(0);
        verify(board).sendToPosition(eq(0), eq(Protocol.TARGETDIRECTION),
            eq("EAST SOUTH "));
    }

    /**
     * Tests the request action method
     */
    @Test
    void testRequestAction() {
        doCallRealMethod().when(board).requestPlayerAction(anyBoolean());
        int turn = board.getTurn();
        board.requestPlayerAction(true);
        verify(board).sendToPosition(eq(turn + 1), eq(Protocol.REQUESTACTION),
            anyString());
        turn = board.getTurn();
        board.getPlayerPositions()[turn].setAlive(false);
        board.setRole(turn, new Hunter());
        board.requestPlayerAction(false);
        verify(board, times(2)).requestPlayerAction(eq(true));
    }


}
