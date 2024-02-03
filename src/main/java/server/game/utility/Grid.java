package server.game.utility;

/**
 * Abstract grid class; Grid5x5 is an implementation of it with size 5
 * Defines a specific Grid; can later be used to add bigger boards
 */
public abstract class Grid {

    protected int size;

    private Position[] playerSpawnPositions;

    /**
     * default constructor for grids
     */
    public Grid(int s) {
        size = s;
    }

    /**
     * Method used to fill the grid with Rooms and code the doors
     *
     * @return returns Room-Grid
     */
    public abstract Room[][] fillBoard();

    /**
     * @return the spawn positions of a grid
     */
    public abstract Position[] playerSpawnPositons();

    /**
     * @return the size of the gris
     */
    public int getSize() {
        return size;
    }
}
