package server.game.utility;

import net.Protocol;
import player.Player;

public class Position {
    private int row;
    private int column;

    private Player player;

    private Role role;

    private int lootCount;

    private int moveCount;

    private boolean alive = true;

    private boolean winner = false;

    /**
     * creates a new position
     *
     * @param r grid row coordinate
     * @param c grid column coordinate
     */
    public Position(int r, int c) {
        row = r;
        column = c;
    }

    /**
     * @return grid column coordinate of this position
     */
    public int getColumn() {
        return column;
    }

    /**
     * @return grid row coordinate of this position
     */
    public int getRow() {
        return row;
    }

    /**
     * set grid coordinates of this position
     *
     * @param r row
     * @param c column
     */
    public void setPosition(int r, int c) {
        row = r;
        column = c;
    }

    /**
     * @param p set player of this position
     */
    public void setPlayer(Player p) {
        this.player = p;
    }

    /**
     * @return player of this position
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @param role sets role of this position
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * @return role of this position
     */
    public Role getRole() {
        return role;
    }

    /**
     * increase loot count on looting action
     */
    public void increaseLoot() {
        lootCount++;
    }

    /**
     * @return the euclidian norm for this Position
     */
    public double norm() {
        return Math.sqrt(this.row * this.row + this.column * this.column);
    }

    /**
     * @return if the coordinates match the other ones
     */
    public boolean equalCoords(Position other) {
        return this.row == other.row && this.column == other.column;
    }

    /**
     * @return if the player is alive
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * sets whether the player is alive
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * @return the amount of chests this player looted
     */
    public int getLootCount() {
        return lootCount;
    }

    /**
     * @return if this player won
     */
    public boolean isWinner() {
        return winner;
    }

    /**
     * set the "winner" status of this Position
     */
    public void setWinner(boolean winner) {
        player.getSocket().send(Protocol.DISPLAY, "YOU WON! congratulations!");
        this.winner = winner;
    }

    /**
     * increase the number of moves this player did
     */
    public void increaseMove() {
        moveCount++;
    }

    /**
     * @return the number of moves this Position did
     */
    public int getMoveCount() {
        return moveCount;
    }
}
