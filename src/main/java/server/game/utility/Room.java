package server.game.utility;

import net.Protocol;

/**
 * Room-Class; used to represent a room-instance, saves the amount of
 * doors either in a boolean array or explicitly; also saves if the
 * room has any chests; and keeps a list of players present in the room
 */
public class Room {

    // Door-Attributes (False = no Door, True = Door)
    private boolean northDoor;
    private boolean eastDoor;
    private boolean southDoor;
    private boolean westDoor;
    private boolean[] doors;
    /**
     * does the room have a chest.
     * 2 if it has a chest and is unlooted, 1 if it has been looted
     * 0 if there is none
     */
    private int chestStatus = 0;

    /**
     * is the room on the edge of the grid; false per default
     */
    private boolean isEdgeRoom = false;


    /**
     * Constructor; setting the door attributes
     * For ease of use: input via integers 0/1
     *
     * @param n north door
     * @param e east door
     * @param s south door
     * @param w west door
     */
    public Room(boolean n, boolean e, boolean s, boolean w) {
        this.northDoor = n;
        this.eastDoor = e;
        this.southDoor = s;
        this.westDoor = w;
        this.chestStatus = 0;
        createDoorArray();
    }

    /**
     * Constructor; setting the door attributes
     * For ease of use: input via integers 0/1
     *
     * @param n        north door
     * @param e        east door
     * @param s        south door
     * @param w        west door
     * @param hasChest a chest exist
     */
    public Room(boolean n, boolean e, boolean s, boolean w, boolean hasChest) {
        this.northDoor = n;
        this.eastDoor = e;
        this.southDoor = s;
        this.westDoor = w;
        if (hasChest) {
            this.chestStatus = 2;
        }
        createDoorArray();
    }

    /**
     * Creates a new Door-Array; May be useful for methods if they
     * want to access the doors in array-form, instead of calling
     * the doors individually
     */
    private void createDoorArray() {
        boolean[] arr = new boolean[4];
        arr[0] = this.northDoor;
        arr[1] = this.eastDoor;
        arr[2] = this.southDoor;
        arr[3] = this.westDoor;
        this.doors = arr;
    }

    /**
     * getter methods
     */
    public boolean isEdgeRoom() {
        return isEdgeRoom;
    }

    /**
     * returns the amount of doors in each room
     *
     * @return door-Number
     */
    public int getAmountOfDoors() {
        int count = 0;
        for (boolean door : this.doors) {
            if (door) {
                count++;
            }
        }
        return count;
    }

    /**
     * returns if there is a chest in the room
     *
     * @return boolean hasChest
     */
    public boolean hasUnlootedChest() {
        return chestStatus == 2;
    }

    /**
     * @return if this room has a chest
     */
    public boolean hasChest() {
        return chestStatus > 0;
    }

    /**
     * @return if this room has a door in direction
     */
    public boolean hasDoor(GameActions.Directions direction) {
        return doors[direction.getIndex()];
    }

    /**
     * loots the chest
     */
    public void loot() {
        chestStatus--;
    }

    /**
     * @return a String containing door and loot info, conforming to the protocol
     */
    @Override
    public String toString() {
        String out = "";
        for (int i = 0; i < 4; i++) {
            if (doors[i]) {
                out = out + GameActions.Directions.directionAtIndex(i) + ",";
            }
        }
        out = out + Protocol.DELIMITER;
        if (hasChest()) {
            out = out + "CHEST,";
            if (hasUnlootedChest()) {
                out = out + "FULL";
            } else {
                out = out + "EMPTY";
            }
        }

        return out;
    }
}
