package server.game.utility;

import java.util.Arrays;

import static java.lang.Math.max;
import static java.lang.Math.abs;


public class GameActions {


    /**
     * enum for direction sides
     */
    public enum Directions {

        /**
         * relative coodinates of north
         */
        NORTH(-1, 0, 0),
        /**
         * relative coodinates of east
         */
        EAST(0, 1, 1),
        /**
         * relative coodinates of south
         */
        SOUTH(1, 0, 2),
        /**
         * relative coodinates of west
         */
        WEST(0, -1, 3);

        private int row;
        private int column;
        private int index;

        Directions(int r, int c, int i) {
            this.row = r;
            this.column = c;
            this.index = i;
        }

        /**
         * @return the row component of this direction
         */
        public int row() {
            return row;
        }

        /**
         * @return the column component of this direction
         */
        public int col() {
            return column;
        }

        /**
         * @return the index of this direction
         */
        public int getIndex() {
            return index;
        }

        /**
         * @return the name of the direction at index i
         */
        public static String directionAtIndex(int i) {
            for (Directions d : Directions.values()) {
                if (d.index == i) {
                    return d.toString();
                }
            }
            return "";
        }

        /**
         * @return the direction of the entered coordinates
         * if the coordinates are absolute this corresponds to the quadrant or axis,
         * for relative coordinates this gives the relative direction
         */
        public static String getDirectionByValue(int row, int col) {
            String compositeDirection = "";
            //The max() is needed to avoid divisions by zero
            int normalizedRow = row / max(abs(row), 1);
            int normalizedCol = col / max(abs(col), 1);
            for (Directions d : Directions.values()) {
                if (d.row == normalizedRow && d.column == 0) {
                    compositeDirection += d.name() + " ";
                }
                if (d.column == normalizedCol && d.row == 0) {
                    compositeDirection += d.name() + " ";
                }
            }
            return compositeDirection;
        }
    }

    /**
     * enum for actions related to game
     */
    public enum Actions {

        /**
         * encodes a move action
         */
        MOVE("move"),

        /**
         * encodes looting or nearest player info
         */
        ROOMACTION("action"),

        /**
         * encodes a turn being skipped
         */
        SKIP("skip"),

        /**
         * only gets used for invalid turns
         */
        INVALID("");

        private String action;

        Actions(String action) {
            this.action = action;
        }

    }

    private Actions action = Actions.INVALID;
    private Directions direction = null;

    /**
     * encodes a game action from a network transmission
     *
     * @throws GameException if no valid action can be generated
     */
    public GameActions(String[] transmission) throws GameException {
        for (Actions a : Actions.values()) {
            if (transmission[1].equalsIgnoreCase(a.action)) {
                this.action = a;
            }
        }
        if (this.action.equals(Actions.MOVE)) {
            for (Directions d : Directions.values()) {
                if (transmission[2].equalsIgnoreCase(d.toString())) {
                    this.direction = d;
                }
            }
        }
        if (this.action.equals(Actions.INVALID) ||
            (this.action.equals(Actions.MOVE) && this.direction == null)) {
            throw new GameException(
                "No matching game action for " + Arrays.toString(transmission));
        }
    }

    /**
     * additional constructor, for unit tests
     */
    public GameActions(Actions a, Directions d) {
        this.action = a;
        if (a.equals(Actions.MOVE)) {
            this.direction = d;
        }

    }

    /**
     * @return the direction
     */
    public Directions getDirection() {
        return direction;
    }

    /**
     * @return the action
     */
    public Actions getAction() {
        return action;
    }

}

