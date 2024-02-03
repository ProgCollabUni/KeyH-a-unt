package server.game;

import net.Protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import player.Player;
import server.Lobby;
import server.game.utility.*;



/**
 * EventHandler handles any events happening in the game, like f.ex.
 * player movement, chest opening, etc...
 */
public class GameHandler {

    /**
     * The board on which the events have to be handled
     */
    private Board board;
    private Lobby lobby;

    private Game game;


    private static Logger logger = LogManager.getRootLogger();

    private int ghostPos;

    /**
     * starts a new EventHandler
     */
    public GameHandler(Board board, Game game) {
        this.board = board;
        this.game = game;
    }


    /**
     * shuffles player list so that Player 1 is at board position 1
     * also chooses the roles of the players
     */
    public void startGame(Lobby l) {
        lobby = l;
        lobby.shufflePlayerList();
        int index = 0;
        ghostPos = 3;
        for (Player p : lobby.getPlayerLobbyList()) {
            board.setPlayerToPosition(index, p);
            if (index == ghostPos) {
                board.setRole(index, new Ghost());
                board.sendToPosition(index, Protocol.GAMEINFO,
                    "You're the Ghost! You may walk through walls!");
                board.sendToPosition(index, Protocol.PLAYERINFO, "ghost");
                logger.info("Player " + p.getNickname() + " is the Ghost!");
            } else {
                board.setRole(index, new Hunter());
                board.sendToPosition(index, Protocol.GAMEINFO,
                    "You're a Hunter, please don't hit your head!");
                board.sendToPosition(index, Protocol.PLAYERINFO, "hunter");
                logger.info("Player " + p.getNickname() + " is a Hunter!");
            }
            board.sendToPosition(index, Protocol.ROOMINFO,
                board.getRoom(index).toString());
            index++;
        }
        logger.info("initialized game positions and roles in lobby " + lobby.getName());
        board.requestPlayerAction(false);
    }

    /**
     * decodes and executes a player turn
     *
     * @param index  index of the playerPosition submitting the turn
     * @param action GameAction containing move and directions
     */
    public void playerAction(int index, GameActions action) {
        boolean gameWon = false;
        try {
            if (index != board.getTurn()) {
                throw new GameException("It is not your turn! please wait!");
            }
            switch (action.getAction()) {
                case MOVE -> {
                    gameWon = board.movePlayer(index, action.getDirection());
                }
                case ROOMACTION -> board.roomAction(index);
                case SKIP ->
                    board.sendToPosition(index, Protocol.GAMEINFO, "you skipped!");
                default -> throw new GameException("no action specified!");
            }
            board.getPlayerPosition(index).increaseMove();
            if (gameWon) {
                game.setWinner(board.getWinnerPlayer());
                lobby.finishGame();
            } else {
                board.requestPlayerAction(true);
            }
        } catch (GameException e) {
            logger.error("invalid move: ", e);
            board.sendToPosition(index, Protocol.GAMEINFO,
                "invalid move: " + e.getMessage());
            board.requestPlayerAction(false);

        } finally {
            board.findNearestPlayer(ghostPos);
            for (int i = 0; i < lobby.getPlayerLobbyList().size(); i++) {
                board.sendToPosition(i, Protocol.ROOMINFO,
                    board.getRoom(i).toString());
            }
        }
    }


}
