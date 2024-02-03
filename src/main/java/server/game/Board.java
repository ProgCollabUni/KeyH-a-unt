package server.game;

import net.Protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import player.Player;
import server.game.utility.GameActions.Directions;
import server.game.utility.GameException;
import server.game.utility.Ghost;
import server.game.utility.Grid;
import server.game.utility.Position;
import server.game.utility.Role;
import server.game.utility.Room;

/**
 * Board/ Maze, where all the rooms are positioned, and also all
 * the players and chests spawn. Has a 2D-Array of rooms
 */
public class Board {

    private static Logger logger = LogManager.getRootLogger();
    private String gameName;
    /**
     * Array of rooms
     */
    private Room[][] rooms;
    private Grid grid;
    private Position[] playerPositions;
    private int turn = 0;

    /**
     * sets up the board with a maze as an argument
     */
    public Board(Grid g, String name) {
        this.grid = g;
        this.playerPositions = g.playerSpawnPositons();
        this.rooms = grid.fillBoard();
        this.gameName = name;
    }

    /**
     * @return the position and roles associated with the lobby player
     */
    public Position getPlayerPosition(int index) {
        return playerPositions[index];
    }

    /**
     * @return the positions and roles associated with the lobby players
     */
    public Position[] getPlayerPositions() {
        return playerPositions;
    }

    /**
     * sets the player associated to the position at index
     */
    public void setPlayerToPosition(int index, Player p) {
        playerPositions[index].setPlayer(p);
    }

    /**
     * sets the role of the Position at index
     */
    public void setRole(int index, Role role) {
        if (role instanceof Ghost) {
            playerPositions[index].setAlive(false);
        }
        playerPositions[index].setRole(role);
    }

    /**
     * moves a player
     *
     * @param index     index in playerPositions[] of the player to move
     * @param direction direction of the move
     * @return if the game was won with this move
     * @throws GameException if the move is invalid (e.g. no door in direction)
     */

    public boolean movePlayer(int index, Directions direction) throws GameException {
        boolean validMoveDone;
        int row = playerPositions[index].getRow();
        int col = playerPositions[index].getColumn();
        //Ghosts can walk through walls and glitch to the other side of the maze
        if (playerPositions[index].getRole() instanceof Ghost) {
            int boundary = grid.getSize();
            int newRow = row + direction.row();
            int newCol = col + direction.col();
            if (newRow < 0) {
                newRow = boundary - 1;
            } else if (newRow == boundary) {
                newRow = 0;
            }
            if (newCol < 0) {
                newCol = boundary - 1;
            } else if (newCol == boundary) {
                newCol = 0;
            }
            playerPositions[index].setPosition(newRow, newCol);
            validMoveDone = true;
            logger.trace("Game " + gameName + ": Ghost " +
                playerPositions[index].getPlayer().getNickname() + " moved from (" + row +
                "," + col + ") to (" + newRow + "," + newCol + ")");
            //Hunters have to stick to doors
        } else if (rooms[row][col].hasDoor(direction)) {
            int newRow = row + direction.row();
            int newCol = col + direction.col();
            playerPositions[index].setPosition(newRow, newCol);
            validMoveDone = true;
            logger.trace("Game " + gameName + ": Hunter " +
                playerPositions[index].getPlayer().getNickname() + " moved from (" + row +
                "," + col + ") to (" + newRow + "," + newCol + ")");

            if (playerPositions[index].getLootCount() > 0) {
                sendPlayerEscapeDirection(index);
            }
        } else {
            if (playerPositions[index].getLootCount() > 0) {
                sendPlayerEscapeDirection(index);
            }
            throw new GameException(
                "no valid door in direction " + direction + " in room " + row + "," +
                    col);
        }
        displaySameRoomPlayers();

        return runEscapeCheck(playerPositions[index]) || runHunterGhostCollisionCheck();
    }

    /**
     * @return true when a hunter reached the exit with loot
     */
    public boolean runEscapeCheck(Position p) {
        int boardCenter = grid.getSize() / 2; //rounds automatically
        if (p.getRow() == boardCenter && p.getColumn() == boardCenter &&
            p.getLootCount() >= 1) {
            p.setWinner(true);
            logger.info(
                "Hunter " + p.getPlayer().getNickname() + "escaped successfully!");
            return true;
        }
        return false;
    }

    /**
     * @return true if all players have been haunted by the ghost
     */
    public boolean runHunterGhostCollisionCheck() {
        Position ghostPos = null;
        for (Position p : playerPositions) {
            if (p.getRole() instanceof Ghost) {
                ghostPos = p;
            }
        }
        boolean allDead = true;
        for (int i = 0; i < playerPositions.length; i++) {
            if (playerPositions[i].equalCoords(ghostPos) &&
                !(playerPositions[i].getRole() instanceof Ghost) &&
                playerPositions[i].isAlive()) {

                playerPositions[i].setAlive(false);
                sendToPosition(i, Protocol.DISPLAY,
                    "YOU GOT SPOOKED! The Ghost caught you!");
                sendToAll(Protocol.GAMEINFO,
                    "Player " + playerPositions[i].getPlayer().getNickname() +
                        " got spooked by the Ghost!");
                logger.info("Hunter " + playerPositions[i].getPlayer().getNickname() +
                    " got caught by the ghost at (" + playerPositions[i].getRow() + "," +
                    playerPositions[i].getColumn() + ")");
            }
            allDead &= !playerPositions[i].isAlive();
        }
        if (allDead) {
            logger.info("all Hunters were dead, Ghost wins");
            ghostPos.setWinner(true);
        }
        return allDead;
    }

    /**
     * executes a room action (looting or player info)
     *
     * @param index index of the playerPosition executing
     * @throws GameException if no loot is available
     */
    public void roomAction(int index) throws GameException {
        int row = playerPositions[index].getRow();
        int col = playerPositions[index].getColumn();
        if (playerPositions[index].getRole() instanceof Ghost) {
            throw new GameException("Ghosts shouldn't have to do Actions!");
        } else {
            if (rooms[row][col].hasUnlootedChest()) {
                rooms[row][col].loot();
                playerPositions[index].increaseLoot();
                logger.trace("Game " + gameName + ": Hunter " +
                    playerPositions[index].getPlayer().getNickname() +
                    " has collected some Loot at (" + row + "," + col + ")");
                sendPlayerEscapeDirection(index);
                sendToPosition(index, Protocol.DISPLAY,
                    "You have collected a key! You are now able to escape!");
                sendToPosition(index, Protocol.GAMEINFO,
                    "You have collected a key! You are now able to escape!");
            } else {
                throw new GameException(
                    "no (lootable) chest found in this room: " + row + "," + col);
            }
        }
    }

    /**
     * checks if players occupy the same field and notifies them in that case
     */
    public void displaySameRoomPlayers() {
        int end = playerPositions.length;
        boolean[] ghostInRoom = new boolean[end];
        int[] playerCount = new int[end];
        for (int i = 0; i < end; i++) {
            int iRow = playerPositions[i].getRow();
            int iCol = playerPositions[i].getColumn();
            Role iRole = playerPositions[i].getRole();
            for (int j = i + 1; j < end; j++) {
                int jRow = playerPositions[j].getRow();
                int jCol = playerPositions[j].getColumn();
                Role jRole = playerPositions[j].getRole();
                if (iRow == jRow && iCol == jCol) {
                    if (jRole instanceof Ghost) {
                        ghostInRoom[i] = true;
                    }
                    playerCount[i]++;
                    if (iRole instanceof Ghost) {
                        ghostInRoom[j] = true;
                    }
                    playerCount[j]++;
                }
            }
        }
        for (int i = 0; i < end; i++) {
            String ghost = String.valueOf(ghostInRoom[i]);
            sendToPosition(i, Protocol.ROOMPLAYER,
                ghost + Protocol.DELIMITER + playerCount[i]);
        }
    }

    /**
     * sends a network transmission
     *
     * @param index index of the playerPosition to send to
     */
    public void sendToPosition(int index, Protocol cmd, String msg) {
        playerPositions[index].getPlayer().getSocket().send(cmd, msg);
    }

    /**
     * @param index index of the playerPosition
     * @return the room of the player at index
     */

    public Room getRoom(int index) {
        int row = playerPositions[index].getRow();
        int col = playerPositions[index].getColumn();
        return rooms[row][col];
    }

    /**
     * gives the ghost hints about the nearest player
     */
    public void findNearestPlayer(int index) {
        Position pos = new Position(grid.getSize(), grid.getSize());
        int row = playerPositions[index].getRow();
        int col = playerPositions[index].getColumn();
        for (Position p : playerPositions) {
            if (p.getRole() instanceof Ghost || !p.isAlive()) {
                continue;
            }
            Position relative = new Position(p.getRow() - row, p.getColumn() - col);
            if (relative.norm() < pos.norm()) {
                pos = relative;
            }
        }
        sendToPosition(index, Protocol.TARGETDIRECTION,
            Directions.getDirectionByValue(pos.getRow(), pos.getColumn()));

        logger.trace("Game " + gameName + ": Ghost " +
            playerPositions[index].getPlayer().getNickname() +
            " asked for player Positions from (" + row + "," + col + ") and got (" +
            pos.getRow() + "," + pos.getColumn() + ") which is " +
            Directions.getDirectionByValue(pos.getRow(), pos.getColumn()));
    }

    /**
     * shows the player the direction of the exit when they have collected loot
     */
    public void sendPlayerEscapeDirection(int index) {
        int center = grid.getSize() / 2;
        int playerRow = playerPositions[index].getRow();
        int playerCol = playerPositions[index].getColumn();
        sendToPosition(index, Protocol.TARGETDIRECTION,
            Directions.getDirectionByValue(center - playerRow, center - playerCol));
    }

    /**
     * requests the next player move.
     */
    public void requestPlayerAction(boolean increment) {
        if (increment) {
            turn++;
            if (turn == playerPositions.length) {
                turn = 0;
            }
        }
        if (playerPositions[turn].isAlive() ||
            playerPositions[turn].getRole() instanceof Ghost) {
            sendToPosition(turn, Protocol.REQUESTACTION, "It's your turn!");
            sendToPosition(turn, Protocol.GAMEINFO, "It's your turn!");
            logger.trace("Game " + gameName + ": requested turn from " +
                playerPositions[turn].getPlayer().getNickname());
        } else {
            requestPlayerAction(true);
        }
    }

    /**
     * @return the index of the player whose turn it is
     */
    public int getTurn() {
        return turn;
    }

    /**
     * @return the Position with the player that won
     * @throws GameException if there is no winner
     */
    public Position getWinnerPlayer() throws GameException {
        for (Position p : playerPositions) {
            if (p.isWinner()) {
                return p;
            }
        }
        throw new GameException("No winner found. Logic must be faulty");
    }

    /**
     * sends a transmission to all participating players
     */
    public void sendToAll(Protocol cmd, String msg) {
        for (Position p : playerPositions) {
            p.getPlayer().getSocket().send(cmd, msg);
        }
    }
}
