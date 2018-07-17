package org.dmw.timer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

public class TimerGame
{
    private static long gameIdSequence = 1L;
    private static final Hashtable<Long, String> pendingGames = new Hashtable<>();
    private static final Map<Long, TimerGame> activeGames = new Hashtable<>();
    private final long id;
    private final String player1;
    private final String player2;
    private Player nextMove = Player.random();
    private boolean over;
    private boolean draw;
    private Player winner;

    private String[] currTime = {"",""};
    private int[] solveCnt = {0,0};
    private int[] winCnt = {0,0};

    private TimerGame(long id, String player1, String player2)
    {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
    }

    public int GetRound(Player player){
        int pid = player == Player.PLAYER1?0:1;
        return solveCnt[pid];
    }

    public int GetScore(Player player){
        int pid = player == Player.PLAYER1?0:1;
        return winCnt[pid];
    }

    public void winning(Player player){
        int pid = player == Player.PLAYER1?0:1;
        winCnt[pid]++;
    }

    public String GetTime(Player player){
        int pid = player == Player.PLAYER1?0:1;
        return currTime[pid];
    }

    public long getId()
    {
        return id;
    }

    public String getPlayer1()
    {
        return player1;
    }

    public String getPlayer2()
    {
        return player2;
    }

    public String getNextMoveBy()
    {
        return nextMove == Player.PLAYER1 ? player1 : player2;
    }

    public boolean isOver()
    {
        return over;
    }

    public boolean isDraw()
    {
        return draw;
    }

    public Player getWinner()
    {
        return winner;
    }



    @JsonIgnore
    public synchronized void move(Player player, String time)
    {
        int pid = player== Player.PLAYER1?0:1;

        currTime[pid] = time;
        solveCnt[pid] ++;
        // 原来有决定下一步谁走的代码 这里不需要


        this.winner = this.calculateWinner();
        if(this.getWinner() != null || this.isDraw())
            this.over = true;
        if(this.isOver())
            TimerGame.activeGames.remove(this.id);
    }

    public synchronized void forfeit(Player player)
    {
        TimerGame.activeGames.remove(this.id);
        this.winner = player == Player.PLAYER1 ? Player.PLAYER2 : Player.PLAYER1;
        this.over = true;
    }

    private Player calculateWinner()
    {
        // TODO
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Map<Long, String> getPendingGames()
    {
        return (Map<Long, String>) TimerGame.pendingGames.clone();
    }

    public static long queueGame(String user1)
    {
        long id = TimerGame.gameIdSequence++;
        TimerGame.pendingGames.put(id, user1);
        return id;
    }

    public static void removeQueuedGame(long queuedId)
    {
        TimerGame.pendingGames.remove(queuedId);
    }

    public static TimerGame startGame(long queuedId, String user2)
    {
        String user1 = TimerGame.pendingGames.remove(queuedId);
        TimerGame game = new TimerGame(queuedId, user1, user2);
        TimerGame.activeGames.put(queuedId, game);
        return game;
    }

    public static TimerGame getActiveGame(long gameId)
    {
        return TimerGame.activeGames.get(gameId);
    }

    public enum Player
    {
        PLAYER1, PLAYER2;

        private static final Random random = new Random();

        private static Player random()
        {
            return Player.random.nextBoolean() ? PLAYER1 : PLAYER2;
        }
    }
}
