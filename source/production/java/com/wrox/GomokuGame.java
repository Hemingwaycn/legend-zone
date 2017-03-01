package com.wrox;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

public class GomokuGame {
    private static long gameIdSequence = 1L;
    private static final Hashtable<Long, String> pendingGames = new Hashtable<>();
    private static final Map<Long, GomokuGame> activeGames = new Hashtable<>();
    private final long id;
    private final String player1;
    private final String player2;
    private Player nextMove = Player.random();
    private boolean over;
    private boolean draw;
    private Player winner;

    public String historyStr = "";


    boolean isNotOutOfBound(int col, int row){
        if(col>=0 && col<=14 && row>=0 && row<=14){
            return true;
        }
        else{
            return false;
        }
    }

    private GomokuGame(long id, String player1, String player2) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
    }


    public long getId() {
        return id;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public String getNextMoveBy() {
        return nextMove == Player.PLAYER1 ? player1 : player2;
    }

    public boolean isOver() {
        return over;
    }

    public boolean isDraw() {
        return draw;
    }

    public Player getWinner() {
        return winner;
    }

    public int chessman[][] = new int[15][15];
    public int result;


    @JsonIgnore
    public synchronized void move(Player player, int row, int col) {

        int pid = player == Player.PLAYER1 ? 1 : 2;

        chessman[row][col] = pid;

        int[] dx = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] dy = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] dir = {0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < 8; i++) {
            int ccol = col;
            int crow = row;
            while (ccol >= 0 && ccol <= 14 && crow >= 0 && crow <= 14 && chessman[crow][ccol] == pid) {
                dir[i]++;
                ccol += dx[i];
                crow += dy[i];
            }
        }

        for (int i = 0; i <= 3; i++) {
            if (dir[i] + dir[i + 4] > 5) {
                result = pid;
                over = true;
                winner = pid==1?Player.PLAYER1:Player.PLAYER2;
            }
        }


        this.nextMove = this.nextMove == Player.PLAYER1 ? Player.PLAYER2 : Player.PLAYER1;


        this.winner = this.calculateWinner();
        if (this.getWinner() != null || this.isDraw())
            this.over = true;
        if (this.isOver())
            GomokuGame.activeGames.remove(this.id);
    }

    public synchronized void forfeit(Player player) {
        GomokuGame.activeGames.remove(this.id);
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
        return (Map<Long, String>) GomokuGame.pendingGames.clone();
    }

    public static long queueGame(String user1) {
        long id = GomokuGame.gameIdSequence++;
        GomokuGame.pendingGames.put(id, user1);
        return id;
    }

    public static void removeQueuedGame(long queuedId) {
        GomokuGame.pendingGames.remove(queuedId);
    }

    public static GomokuGame startGame(long queuedId, String user2) {
        String user1 = GomokuGame.pendingGames.remove(queuedId);
        GomokuGame game = new GomokuGame(queuedId, user1, user2);
        GomokuGame.activeGames.put(queuedId, game);
        return game;
    }

    public static GomokuGame getActiveGame(long gameId) {
        return GomokuGame.activeGames.get(gameId);
    }

    public enum Player {
        PLAYER1, PLAYER2;

        private static final Random random = new Random();

        private static Player random() {
            return Player.random.nextBoolean() ? PLAYER1 : PLAYER2;
        }
    }
}
