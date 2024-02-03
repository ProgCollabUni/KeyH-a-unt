package server.game.utility;

import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;


import static org.junit.jupiter.api.Assertions.*;

/**
 * We test the Game Action class
 */
class GameActionsTest {

    /**
     * Test done from direction comparing to the origin, if that was intented
     */
    @Test
    void getDirectionByValue() {
        //standard test
        int row = 0;
        int column = 0;
        String out = GameActions.Directions.getDirectionByValue(row, column);
        assertEquals("", out.trim());

        row = 0;
        column = 1;
        out = GameActions.Directions.getDirectionByValue(row, column);
        assertEquals("EAST", out.trim());

        row = 1;
        column = 0;
        out = GameActions.Directions.getDirectionByValue(row, column);
        assertEquals("SOUTH", out.trim());

        row = -1;
        column = 0;
        out = GameActions.Directions.getDirectionByValue(row, column);
        assertEquals("NORTH", out.trim());

        row = 0;
        column = -1;
        out = GameActions.Directions.getDirectionByValue(row, column);
        assertEquals("WEST", out.trim());

        row = 1;
        column = 1;
        out = GameActions.Directions.getDirectionByValue(row, column);
        assertEquals("EAST SOUTH", out.trim());

        row = 1;
        column = -1;
        out = GameActions.Directions.getDirectionByValue(row, column);
        assertEquals("SOUTH WEST", out.trim());

        row = -1;
        column = 1;
        out = GameActions.Directions.getDirectionByValue(row, column);
        assertEquals("NORTH EAST", out.trim());

        row = Integer.MAX_VALUE;
        column = Integer.MAX_VALUE;
        out = GameActions.Directions.getDirectionByValue(row, column);
        assertEquals("EAST SOUTH", out.trim());

        row = 5;
        column = -5;
        out = GameActions.Directions.getDirectionByValue(row, column);
        assertEquals("SOUTH WEST", out.trim());

        row = Integer.MAX_VALUE;
        column = Integer.MIN_VALUE + 1;
        out = GameActions.Directions.getDirectionByValue(row, column);
        assertEquals("SOUTH WEST", out.trim());

    }

    /**
     * We test if we get the direction at index correctly
     */
    @Test
    public void testDirectionAtIndex() {
        assertEquals("NORTH", GameActions.Directions.directionAtIndex(0));
        assertEquals("", GameActions.Directions.directionAtIndex(6));
    }

    /**
     * We test if we can construct the Game Action correctly
     */
    @Test
    public void testConstructGameAction() {
        GameActions[] expected = new GameActions[2];
        expected[0] =
            new GameActions(GameActions.Actions.MOVE, GameActions.Directions.NORTH);
        expected[1] =
            new GameActions(GameActions.Actions.ROOMACTION, GameActions.Directions.EAST);
        final GameActions[] decoded = new GameActions[2];
        assertDoesNotThrow(
            () -> decoded[0] = new GameActions(new String[] {"GMAC", "MOVE", "NORTH"}));
        assertDoesNotThrow(
            () -> decoded[1] = new GameActions(new String[] {"GMAC", "ACTION"}));

        assertThrows(GameException.class,
            () -> new GameActions(new String[] {"GMAC", "MVOE", "NORTH"}));
        assertThrows(GameException.class,
            () -> new GameActions(new String[] {"GMAC", "MOVE", ""}));

        assertTrue(new ReflectionEquals(expected[0]).matches(decoded[0]));
        assertTrue(new ReflectionEquals(expected[1]).matches(decoded[1]));
    }
}
