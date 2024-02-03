package server.game;

import server.Lobby;
import server.Server;
import server.game.utility.*;


/**
 * Used to represent each running game-instance
 */
public class Game {

    /**
     * saves the board/ maze
     */
    private Board board;

    /**
     * Used to handle any events happening in the game, f.ex.
     * a player movement, or a chest opening
     */
    private GameHandler handler;

    private GameState state = GameState.WAITING;
    private Role winnerRole = null;
    private String winnerName;

    private int winnerMoveCount;

    /**
     * Constructor to make a new game
     */
    public Game(String name) {
        /* the board selects the maze this game will run on.
        for now, we only have the hard-coded 5x5 grid */
        this.board = new Board(new Grid5x5(), name);
        this.handler = new GameHandler(board, this);
    }

    /**
     * launches the start sequence for this game and lobby
     */
    public void start(Lobby l) {
        handler.startGame(l);
        this.state = GameState.RUNNING;
    }

    /**
     * pass a player move to the EventHandler for execution
     *
     * @param index  index of the playerPosition submitting the move
     * @param action type of action with direction
     */
    public void playerMove(int index, GameActions action) {
        handler.playerAction(index, action);
    }

    /**
     * @return if the game is currently running
     */
    public boolean isRunning() {
        return state.equals(GameState.RUNNING);
    }

    /**
     * sets the winner info of this game
     */
    public void setWinner(Position winner) {
        this.state = GameState.FINISHED;
        this.winnerRole = winner.getRole();
        this.winnerName = winner.getPlayer().getNickname();
        this.winnerMoveCount = winner.getMoveCount();
        Server.insertHighScore(this);
    }

    /**
     * @return a string containing winner info for highscores
     */
    public String getWinner() {
        if (state.equals(GameState.FINISHED)) {
            return winnerMoveCount + " " + winnerName + " " + winnerRole;
        }
        return "";
    }

    /**
     * @return a String containing the winner, in a nice format
     */
    public String printWinner() {
        if (state.equals(GameState.FINISHED)) {
            return "The game was won by " + winnerName + " as " + winnerRole + " in " +
                winnerMoveCount + " moves!";
        }
        return "";
    }

    /**
     * @return the state of this game as a String
     */
    public String getState() {
        return state.toString();
    }

    /**
     * sets the game state to aborted if somebody logs out while it's running
     */
    public void processLogout() {
        this.state = GameState.ABORTED;
    }
}

enum GameState {
    WAITING(), RUNNING(), FINISHED(), ABORTED()
}
