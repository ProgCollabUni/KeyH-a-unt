package server.game.utility;

/**
 * Used for hardcoding/ filling a 5x5 Maze
 */
public class Grid5x5 extends Grid {

    private final Room[][] rooms;

    /**
     * Constructor, which sets the room-grid.
     */
    public Grid5x5() {
        super(5);
        this.rooms = new Room[size][size];
    }

    /**
     * Used to fill the Board with a pre-defined Maze / Room structure
     * returns the filled Board
     */
    @Override
    public Room[][] fillBoard() {

        // Hardcoding here

        // Row 0 (Top row of the board)
        rooms[0][0] = new Room(false, true, true, false);
        rooms[0][1] = new Room(false, false, true, true);
        rooms[0][2] = new Room(false, false, true, false, true);
        rooms[0][3] = new Room(false, true, true, false);
        rooms[0][4] = new Room(false, false, true, true);

        // Row 1
        rooms[1][0] = new Room(true, true, true, false);
        rooms[1][1] = new Room(true, true, true, true);
        rooms[1][2] = new Room(true, true, true, true);
        rooms[1][3] = new Room(true, true, false, true);
        rooms[1][4] = new Room(true, false, false, true);

        // Row 2
        rooms[2][0] = new Room(true, true, false, false);
        rooms[2][1] = new Room(true, true, true, true, true);
        rooms[2][2] = new Room(true, true, false, true);
        rooms[2][3] = new Room(false, true, true, true);
        rooms[2][4] = new Room(false, false, false, true, true);

        // Row 3
        rooms[3][0] = new Room(false, true, true, false);
        rooms[3][1] = new Room(true, true, false, true, true);
        rooms[3][2] = new Room(false, true, true, true);
        rooms[3][3] = new Room(true, true, false, true);
        rooms[3][4] = new Room(false, false, true, true);

        // Row 4
        rooms[4][0] = new Room(true, true, false, false);
        rooms[4][1] = new Room(false, true, false, true);
        rooms[4][2] = new Room(true, true, false, true);
        rooms[4][3] = new Room(false, true, false, true);
        rooms[4][4] = new Room(true, false, false, true);

        return rooms;
    }

    @Override
    public Position[] playerSpawnPositons() {
        return new Position[] {new Position(0, 0), new Position(0, 4), new Position(4, 0),
            new Position(4, 4)};
    }

}
