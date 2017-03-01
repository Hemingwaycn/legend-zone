package com.wrox;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

@ServerEndpoint("/gomoku/{gameId}/{username}")
public class GomokuServer {
    private static Map<Long, Game> games = new Hashtable<>();
    private static ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") long gameId,
                       @PathParam("username") String username) {
        try {
            GomokuGame gomokuGame = GomokuGame.getActiveGame(gameId);
            if (gomokuGame != null) {
                session.close(new CloseReason(
                        CloseReason.CloseCodes.UNEXPECTED_CONDITION,
                        "This game has already started."
                ));
            }

            List<String> actions = session.getRequestParameterMap().get("action");
            if (actions != null && actions.size() == 1) {
                String action = actions.get(0);
                if ("start".equalsIgnoreCase(action)) {
                    Game game = new Game();
                    game.gameId = gameId;
                    game.player1 = session;
                    GomokuServer.games.put(gameId, game);
                } else if ("join".equalsIgnoreCase(action)) {
                    Game game = GomokuServer.games.get(gameId);
                    game.player2 = session;
                    game.gomokuGame = GomokuGame.startGame(gameId, username);
                    this.sendJsonMessage(game.player1, game,
                            new GameStartedMessage(game.gomokuGame));
                    this.sendJsonMessage(game.player2, game,
                            new GameStartedMessage(game.gomokuGame));
                } else if ("vsAI".equalsIgnoreCase(action)) {
                    Game game = new Game();
                    game.vsAI = true;
                    game.gameId = gameId;
                    game.player1 = session;
                    game.gomokuGame = GomokuGame.startGame(gameId, "Legend's Bot");
                    GomokuServer.games.put(gameId, game);
                    this.sendJsonMessage(game.player1, game,
                            new GameStartedMessage(game.gomokuGame));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                session.close(new CloseReason(
                        CloseReason.CloseCodes.UNEXPECTED_CONDITION, e.toString()
                ));
            } catch (IOException ignore) {
            }
        }
    }

    @OnMessage
    public void onMessage(Session session, String message,
                          @PathParam("gameId") long gameId) {
        Game game = GomokuServer.games.get(gameId);
        boolean isPlayer1 = session == game.player1;

        try {
            Move move = GomokuServer.mapper.readValue(message, Move.class);
            game.gomokuGame.move(
                    isPlayer1 ? GomokuGame.Player.PLAYER1 :
                            GomokuGame.Player.PLAYER2,
                    move.getRow(),
                    move.getColumn()
            );

            if (game.gomokuGame.isOver()) {
                if (game.gomokuGame.isDraw()) {
                    this.sendJsonMessage(game.player1, game,
                            new TextMessage("游戏结束,平局."));
                    this.sendJsonMessage(game.player2, game,
                            new TextMessage("游戏结束,平局."));
                    this.sendJsonMessage(game.player1, game,
                            new GameIsDrawMessage());
                    this.sendJsonMessage(game.player2, game,
                            new GameIsDrawMessage());

                } else {
                    boolean wasPlayer1 = game.gomokuGame.getWinner() ==
                            GomokuGame.Player.PLAYER1;



                    this.sendJsonMessage(game.player1, game,
                            new GameOverMessage(wasPlayer1));
                    this.sendJsonMessage(game.player2, game,
                            new GameOverMessage(!wasPlayer1));
                }
                game.player1.close();
                if (!game.vsAI) game.player2.close();
                return;
            }

            System.out.println("你下在 "+move.getRow()+","+move.getColumn());

            game.gomokuGame.historyStr += (move.getRow()) + "," + (move.getColumn()) + ",2\n";

            if (isPlayer1) {


                if (game.vsAI) {
                    // 这里
                    String para = "BOARD\n"+game.gomokuGame.historyStr+"DONE\nEND";
                    System.out.println("para is "+ para);
                    //String resp = CppRunner.RunCpp("C:\\Users\\oo\\Desktop\\testdev\\gaga", para);
                    //
                    String resp = CppRunner.RunCpp("/Share/huangzc/legend-zone/wine", para);

                    int aicol;
                    int airow;

                    String xy[] = resp.split(",");
                    airow = Integer.parseInt(xy[0]);
                    aicol = Integer.parseInt(xy[1]);

                    game.gomokuGame.historyStr += (airow) + "," + (aicol) + ",2\n";

                    System.out.println(resp);

                    /*GomokuAI ai = new GomokuAI();
                    int aivalue = ai.aimove(game.gomokuGame);
                    aicol = aivalue%15;
                    airow = aivalue/15;
*/



                    System.out.println("ai 下在"+airow+","+aicol);

                    Move aiMove = new Move();
                    aiMove.setColumn(aicol);
                    aiMove.setRow(airow);

                    game.gomokuGame.move(
                            GomokuGame.Player.PLAYER2,
                            airow,
                            aicol
                    );
                    this.sendJsonMessage(game.player1, game,
                            new OpponentMadeMoveMessage(aiMove));

                }

            } else {

            }





            if (game.gomokuGame.isOver()) {
                if (game.gomokuGame.isDraw()) {
                    this.sendJsonMessage(game.player1, game,
                            new TextMessage("游戏结束,平局."));
                    this.sendJsonMessage(game.player2, game,
                            new TextMessage("游戏结束,平局."));
                    this.sendJsonMessage(game.player1, game,
                            new GameIsDrawMessage());
                    this.sendJsonMessage(game.player2, game,
                            new GameIsDrawMessage());

                } else {
                    boolean wasPlayer1 = game.gomokuGame.getWinner() ==
                            GomokuGame.Player.PLAYER1;



                    this.sendJsonMessage(game.player1, game,
                            new GameOverMessage(wasPlayer1));
                    this.sendJsonMessage(game.player2, game,
                            new GameOverMessage(!wasPlayer1));
                }
                game.player1.close();
                if (!game.vsAI) game.player2.close();
            }


        } catch (IOException e) {
            this.handleException(e, game);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("gameId") long gameId) {
        Game game = GomokuServer.games.get(gameId);
        if (game == null)
            return;
        boolean isPlayer1 = session == game.player1;
        if (game.gomokuGame == null) {
            GomokuGame.removeQueuedGame(game.gameId);
        } else if (!game.gomokuGame.isOver()) {
            game.gomokuGame.forfeit(isPlayer1 ? GomokuGame.Player.PLAYER1 :
                    GomokuGame.Player.PLAYER2);
            Session opponent = (isPlayer1 ? game.player2 : game.player1);
            this.sendJsonMessage(opponent, game, new GameForfeitedMessage());
            try {
                opponent.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendJsonMessage(Session session, Game game, Message message) {
        if (session == null) {
            return;
        }
        try {
            session.getBasicRemote()
                    .sendText(GomokuServer.mapper.writeValueAsString(message));
        } catch (IOException e) {
            this.handleException(e, game);
        }
    }

    private void handleException(Throwable t, Game game) {
        t.printStackTrace();
        String message = t.toString();
        try {
            game.player1.close(new CloseReason(
                    CloseReason.CloseCodes.UNEXPECTED_CONDITION, message
            ));
        } catch (IOException ignore) {
        }
        try {
            game.player2.close(new CloseReason(
                    CloseReason.CloseCodes.UNEXPECTED_CONDITION, message
            ));
        } catch (IOException ignore) {
        }
    }

    private static class Game {
        public long gameId;

        public Session player1;

        public Session player2;

        public GomokuGame gomokuGame;

        public boolean vsAI = false;
    }

    public static class Move {
        private int row;

        private int column;

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }
    }

    public static abstract class Message {
        private final String action;
        private final String msg;

        public Message(String action, String msg) {
            this.action = action;
            this.msg = msg;
        }

        public String getAction() {
            return this.action;
        }

        public String getMsg() {
            return this.msg;
        }
    }

    public static class GameStartedMessage extends Message {
        private final GomokuGame game;

        public GameStartedMessage(GomokuGame game) {
            super("gameStarted", "");
            this.game = game;
        }

        public GomokuGame getGame() {
            return game;
        }
    }

    public static class OpponentMadeMoveMessage extends Message {
        private final Move move;

        public OpponentMadeMoveMessage(Move move) {
            super("opponentMadeMove", "");
            this.move = move;
        }

        public Move getMove() {
            return move;
        }
    }

    public static class GameOverMessage extends Message {
        private final boolean winner;

        public GameOverMessage(boolean winner) {
            super("gameOver", "");
            this.winner = winner;
        }

        public boolean isWinner() {
            return winner;
        }
    }

    public static class GameIsDrawMessage extends Message {
        public GameIsDrawMessage() {
            super("gameIsDraw", "");
        }
    }

    public static class GameForfeitedMessage extends Message {
        public GameForfeitedMessage() {
            super("gameForfeited", "");
        }
    }

    public static class TextMessage extends Message {
        public TextMessage(String msg) {
            super("text", msg);
        }
    }
}



class GomokuAI{
    public int aimove(GomokuGame state2){
        int scoremax = -1, rn = 0;
        int score[][] = new int[15][15];

        for(int i=0; i<=14; i++){
            for(int j=0; j<=14; j++){
                if(state2.chessman[i][j] == 0){
                    score[i][j] = aiscore(state2, i, j);
                    if(score[i][j] > scoremax){
                        scoremax = score[i][j];
                        rn = i*15+j;
                    }
                }
                else if(state2.chessman[i][j] != 0){
                    score[i][j] = -1;
                }
            }
        }

        return rn;
    }

    public int aiscore(GomokuGame state3, int row, int col){
        int rvalue = 0;
        int p1500 = 200, p1400 = 95, p1401 = 15, p1410 = 95, p1411 = 15, p1300 = 20, p1301 = 0, p1310 = 20, p1311 = 0, p1200 = 4, p1201 = 0, p1210 = 4, p1211 = 0, p1100 = 1, p1101 = 0;
        int p0500 = 150, p0400 = 85, p0401 = 15, p0410 = 85, p2411 = 15, p0300 = 10, p0301 = 0, p0310 = 10, p0311 = 0, p0200 = 3, p0201 = 0, p0210 = 3, p0211 = 0, p0100 = 0, p0101 = 0;
        int[] dx = {0,1,1,1,0,-1,-1,-1};
        int[] dy = {-1,-1,0,1,1,1,0,-1};
        int[] dir = {0,0,0,0,0,0,0,0};
        int[] dir1b = {0,0,0,0,0,0,0,0};

        state3.chessman[row][col] = 1;

        for(int i=0; i<8; i++){
            int crow = row;
            int ccol = col;
            while(state3.isNotOutOfBound(crow, ccol) && state3.chessman[crow][ccol] == 1){
                dir[i]++;
                dir1b[i]++;
                crow += dx[i];
                ccol += dy[i];
            }
            if(state3.isNotOutOfBound(crow, ccol) && state3.chessman[crow][ccol] == 0){
                crow += dx[i];
                ccol += dy[i];
                while(state3.isNotOutOfBound(crow, ccol) && state3.chessman[crow][ccol] == 1){
                    dir1b[i]++;
                    crow += dx[i];
                    ccol += dy[i];
                }
            }
        }

        for(int i=0; i<=3; i++){
            if(dir[i] + dir[i+4] > 5){
                rvalue += p0500;
            }
            else if(dir[i] + dir[i+4] > 4 && state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 0 && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 0){
                rvalue += p0400;
            }
            else if(dir[i] + dir[i+4] > 4 && state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 0 && ((state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4]) && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 1) || (! state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4])))){
                rvalue += p0401;
            }
            else if(dir[i] + dir[i+4] > 4 && ((state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 1) || (! state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]))) && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 0 && state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4])){
                rvalue += p0401;
            }
            else if(dir1b[i] + dir1b[i+4] > 4 && state3.isNotOutOfBound(row+dx[i]*dir1b[i], col+dy[i]*dir1b[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir1b[i+4], col+dy[i+4]*dir1b[i+4]) && state3.chessman[row+dx[i]*dir1b[i]][col+dy[i]*dir1b[i]] == 0 && state3.chessman[row+dx[i+4]*dir1b[i+4]][col+dy[i+4]*dir1b[i+4]] == 0){
                rvalue += p0410;
            }

            else if(dir[i] + dir[i+4] > 3 && state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 0 && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 0){
                rvalue += p0300;
            }
            else if(dir1b[i] + dir1b[i+4] > 3 && state3.isNotOutOfBound(row+dx[i]*dir1b[i], col+dy[i]*dir1b[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir1b[i+4], col+dy[i+4]*dir1b[i+4]) && state3.chessman[row+dx[i]*dir1b[i]][col+dy[i]*dir1b[i]] == 0 && state3.chessman[row+dx[i+4]*dir1b[i+4]][col+dy[i+4]*dir1b[i+4]] == 0){
                rvalue += p0310;
            }

            else if(dir[i] + dir[i+4] > 2 && state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 0 && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 0){
                rvalue += p0200;
            }
            else if(dir[i] + dir[i+4] == 2 && state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 0 && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 0){
                rvalue += p0100;
            }
        }

        state3.chessman[row][col] = 2;

        for(int i=0; i<8; i++){
            dir[i] = 0;
        }
        for(int i=0; i<8; i++){
            int crow = row;
            int ccol = col;
            while(state3.isNotOutOfBound(crow, ccol) && state3.chessman[crow][ccol] == 2){
                dir[i]++;
                crow += dx[i];
                ccol += dy[i];
            }
        }

        for(int i=0; i<=3; i++){
            if(dir[i] + dir[i+4] > 5){
                rvalue += p1500;
            }
            else if(dir[i] + dir[i+4] > 4 && state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 0 && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 0){
                rvalue += p1400;
            }
            else if(dir[i] + dir[i+4] > 4 && state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 0 && ((state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4]) && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 2) || (! state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4])))){
                rvalue += p1401;
            }
            else if(dir[i] + dir[i+4] > 4 && ((state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 2) || (! state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]))) && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 0 && state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4])){
                rvalue += p1401;
            }
            else if(dir1b[i] + dir1b[i+4] > 4 && state3.isNotOutOfBound(row+dx[i]*dir1b[i], col+dy[i]*dir1b[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir1b[i+4], col+dy[i+4]*dir1b[i+4]) && state3.chessman[row+dx[i]*dir1b[i]][col+dy[i]*dir1b[i]] == 0 && state3.chessman[row+dx[i+4]*dir1b[i+4]][col+dy[i+4]*dir1b[i+4]] == 0){
                rvalue += p1410;
            }

            else if(dir[i] + dir[i+4] > 3 && state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 0 && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 0){
                rvalue += p1300;
            }
            else if(dir1b[i] + dir1b[i+4] > 3 && state3.isNotOutOfBound(row+dx[i]*dir1b[i], col+dy[i]*dir1b[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir1b[i+4], col+dy[i+4]*dir1b[i+4]) && state3.chessman[row+dx[i]*dir1b[i]][col+dy[i]*dir1b[i]] == 0 && state3.chessman[row+dx[i+4]*dir1b[i+4]][col+dy[i+4]*dir1b[i+4]] == 0){
                rvalue += p1410;
            }

            else if(dir[i] + dir[i+4] > 2 && state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 0 && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 0){
                rvalue += p1200;
            }
            else if(dir[i] + dir[i+4] == 2 && state3.isNotOutOfBound(row+dx[i]*dir[i], col+dy[i]*dir[i]) && state3.isNotOutOfBound(row+dx[i+4]*dir[i+4], col+dy[i+4]*dir[i+4]) && state3.chessman[row+dx[i]*dir[i]][col+dy[i]*dir[i]] == 0 && state3.chessman[row+dx[i+4]*dir[i+4]][col+dy[i+4]*dir[i+4]] == 0){
                rvalue += p1100;
            }
        }

        state3.chessman[row][col] = 0;

        return rvalue;
    }
}