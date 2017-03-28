package org.dmw.mafia;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

public class MafiaGame {
    private static long gameIdSequence = 1L;
    private static final Hashtable<Long, String> pendingGames = new Hashtable<>();
    private static final Map<Long, MafiaGame> activeGames = new Hashtable<>();
    private final long id;
    public List<String> playerList;

    private boolean over;

    private Player winner;


    public MafiaGame(long id) {
        this.id = id;
        this.playerList = new ArrayList<>();
    }


    public long getId() {
        return id;
    }

    public String getPlayer(int id) {
        return playerList.get(id);
    }


    public boolean isOver() {
        return over;
    }

    public Player getWinner() {
        return winner;
    }


    public int result;


    @JsonIgnore
    public synchronized void move(Player player, int row, int col) {

    }

    public synchronized void forfeit(Player player) {
        MafiaGame.activeGames.remove(this.id);
        this.winner = player == Player.PLAYER1 ? Player.PLAYER2 : Player.PLAYER1;
        this.over = true;
    }

    private Player calculateWinner() {
        if (result == 1) {
            return Player.PLAYER1;
        } else if (result == 2) {
            return Player.PLAYER2;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Map<Long, String> getPendingGames() {
        return (Map<Long, String>) MafiaGame.pendingGames.clone();
    }

    public static long queueGame(String user1) {
        long id = MafiaGame.gameIdSequence++;
        MafiaGame.pendingGames.put(id, user1);
        return id;
    }

    public static void removeQueuedGame(long queuedId) {
        MafiaGame.pendingGames.remove(queuedId);
    }

    public static MafiaGame startGame(long queuedId) {
        String user1 = MafiaGame.pendingGames.remove(queuedId);
        MafiaGame game = new MafiaGame(queuedId);
        MafiaGame.activeGames.put(queuedId, game);
        return game;
    }

    public static void removeGame(long queuedId) {
        MafiaGame.pendingGames.remove(queuedId);
    }

    public static MafiaGame getActiveGame(long gameId) {
        return MafiaGame.activeGames.get(gameId);
    }

    public enum Player {
        PLAYER1, PLAYER2;

        private static final Random random = new Random();

        private static Player random() {
            return Player.random.nextBoolean() ? PLAYER1 : PLAYER2;
        }
    }
}
