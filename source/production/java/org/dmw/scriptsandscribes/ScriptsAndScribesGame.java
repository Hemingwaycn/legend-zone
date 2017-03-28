package org.dmw.scriptsandscribes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

public class ScriptsAndScribesGame
{
    private static long gameIdSequence = 1L;
    private static final Hashtable<Long, String> pendingGames = new Hashtable<>();
    private static final Map<Long, ScriptsAndScribesGame> activeGames = new Hashtable<>();
    private final long id;
    private final String player1;
    private final String player2;
    private Player nextMove = Player.random();
    //private Player[][] grid = new Player[3][9];
    private boolean over;
    private boolean draw;
    private Player winner;

    private int[][] historyMoves = {{-1,-1,-1,-1,-1,-1,-1,-1,-1},{-1,-1,-1,-1,-1,-1,-1,-1,-1}};

    public int GetMove(Player player,int x){
        x--;
        if(player == Player.PLAYER1){
            return historyMoves[0][x];
        }else{
            return historyMoves[1][x];
        }
    }



    private ScriptsAndScribesGame(long id, String player1, String player2)
    {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
    }

    public int GetRound(Player player){
        int pid = player == Player.PLAYER1?0:1;

        for(int i=0;i<9;i++){
            if(historyMoves[pid][i] == -1){
                return i;
            }
        }
        return 9;
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
    public synchronized void move(Player player, int row, int column)
    {
        //if(player != this.nextMove)
        //    throw new IllegalArgumentException("It is not your turn!");

        //if(row > 2 || column > 2)
        //    throw new IllegalArgumentException("Row and column must be 0-3.");

        int pid = player== Player.PLAYER1?0:1;

        for(int i=0;i<9;i++){
            if(historyMoves[pid][i] == -1){
                historyMoves[pid][i] = column+1;
                break;
            }else if(historyMoves[pid][i] == column+1){
                throw new IllegalArgumentException("Number "+ column+1 + " was already used.");
            }
        }


        int r1 = GetRound(Player.PLAYER1);
        int r2 = GetRound(Player.PLAYER2);

        if( r1 == r2 ){
            int n1 = GetMove(ScriptsAndScribesGame.Player.PLAYER1,r1);
            int n2 = GetMove(ScriptsAndScribesGame.Player.PLAYER2,r2);
            if(n1>n2){
                this.nextMove = Player.PLAYER1;
            }else if(n1<n2){
                this.nextMove = Player.PLAYER2;
            }else{

            }
        }else{
            this.nextMove =
                    this.nextMove == Player.PLAYER1 ? Player.PLAYER2 : Player.PLAYER1;
        }

        this.winner = this.calculateWinner();
        if(this.getWinner() != null || this.isDraw())
            this.over = true;
        if(this.isOver())
            ScriptsAndScribesGame.activeGames.remove(this.id);
    }

    public synchronized void forfeit(Player player)
    {
        ScriptsAndScribesGame.activeGames.remove(this.id);
        this.winner = player == Player.PLAYER1 ? Player.PLAYER2 : Player.PLAYER1;
        this.over = true;
    }

    private Player calculateWinner()
    {
        if(historyMoves[0][8] == -1){
            return null;
        }
        if(historyMoves[1][8] == -1){
            return null;
        }

        int p1win = 0;
        for(int i=0;i<9;i++){
            if(historyMoves[0][i]>historyMoves[1][i]){
                p1win++;
            }
            if(historyMoves[0][i]<historyMoves[1][i]){
                p1win--;
            }
        }

        if(p1win>0){
            return Player.PLAYER1;
        }else if(p1win<0){
            return Player.PLAYER2;
        }else{
            draw = true;
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<Long, String> getPendingGames()
    {
        return (Map<Long, String>) ScriptsAndScribesGame.pendingGames.clone();
    }

    public static long queueGame(String user1)
    {
        long id = ScriptsAndScribesGame.gameIdSequence++;
        ScriptsAndScribesGame.pendingGames.put(id, user1);
        return id;
    }

    public static void removeQueuedGame(long queuedId)
    {
        ScriptsAndScribesGame.pendingGames.remove(queuedId);
    }

    public static ScriptsAndScribesGame startGame(long queuedId, String user2)
    {
        String user1 = ScriptsAndScribesGame.pendingGames.remove(queuedId);
        ScriptsAndScribesGame game = new ScriptsAndScribesGame(queuedId, user1, user2);
        ScriptsAndScribesGame.activeGames.put(queuedId, game);
        return game;
    }

    public static ScriptsAndScribesGame getActiveGame(long gameId)
    {
        return ScriptsAndScribesGame.activeGames.get(gameId);
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
