package org.dmw.timer;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

@ServerEndpoint("/timer/{gameId}/{username}")
public class TimerServer {
    private static Map<Long, Game> games = new Hashtable<>();
    private static ObjectMapper mapper = new ObjectMapper();

    private String genScramble(){
        String str = "";
        String[] face = {"U","D","L","R","F","B"};
        String[] deg = {" ","' ","2 "};
        Random rand = new Random();
        int pre = 999;
        int prepre = 999;
        for(int i=0;i<20;i++){
            int cur = rand.nextInt(6);
            while(cur == pre || ( (cur>>1)==(pre>>1) && cur==prepre )){
                cur = rand.nextInt(6);
            }
            str += face[cur];
            str += deg[rand.nextInt(3)];

            prepre = pre;
            pre = cur;
        }
        return str;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") long gameId,
                       @PathParam("username") String username) {
        try {
            TimerGame timerGame = TimerGame.getActiveGame(gameId);
            if (timerGame != null) {
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
                    TimerServer.games.put(gameId, game);
                } else if ("join".equalsIgnoreCase(action)) {
                    Game game = TimerServer.games.get(gameId);
                    game.player2 = session;
                    game.timerGame = TimerGame.startGame(gameId, username);

                    // TODO
                    String str = genScramble();

                    this.sendJsonMessage(game.player1, game,
                            new GameStartedMessage(game.timerGame,str));
                    this.sendJsonMessage(game.player2, game,
                            new GameStartedMessage(game.timerGame,str));
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
        Game game = TimerServer.games.get(gameId);
        boolean isPlayer1 = session == game.player1;

        try {
            Move move = TimerServer.mapper.readValue(message, Move.class);
            game.timerGame.move(
                    isPlayer1 ? TimerGame.Player.PLAYER1 :
                            TimerGame.Player.PLAYER2,
                    move.getTime()

            );

            int r1 = game.timerGame.GetRound(TimerGame.Player.PLAYER1);
            int r2 = game.timerGame.GetRound(TimerGame.Player.PLAYER2);

            int R = r1 > r2 ? r1 : r2;

/*
            if (isPlayer1) {
                this.sendJsonMessage(game.player1, game,
                        new TextMessage("solve " + R + ": 你的成绩 "+ game.timerGame.GetTime(TimerGame.Player.PLAYER1)));
                this.sendJsonMessage(game.player2, game,
                        new TextMessage("solve " + R + ": 对手的成绩 "+ game.timerGame.GetTime(TimerGame.Player.PLAYER1)));
            } else {
                this.sendJsonMessage(game.player2, game,
                        new TextMessage("solve " + R + ": 你的成绩 "+ game.timerGame.GetTime(TimerGame.Player.PLAYER2)));
                this.sendJsonMessage(game.player1, game,
                        new TextMessage("solve " + R + ": 对手的成绩 "+ game.timerGame.GetTime(TimerGame.Player.PLAYER2)));
            }
*/

            if (r1 == r2) {
                //TODO
                this.sendJsonMessage(game.player2, game,
                        new ResultMessage(game.timerGame.GetTime(TimerGame.Player.PLAYER2),game.timerGame.GetTime(TimerGame.Player.PLAYER1)));
                this.sendJsonMessage(game.player1, game,
                        new ResultMessage(game.timerGame.GetTime(TimerGame.Player.PLAYER1),game.timerGame.GetTime(TimerGame.Player.PLAYER2)));

                String str = genScramble();

                this.sendJsonMessage(game.player1, game,
                        new OpponentMadeMoveMessage(move,str));
                this.sendJsonMessage(game.player2, game,
                        new OpponentMadeMoveMessage(move,str));

                if( game.timerGame.GetTime(TimerGame.Player.PLAYER1).compareTo(game.timerGame.GetTime(TimerGame.Player.PLAYER2)) < 0 ){
                    game.timerGame.winning(TimerGame.Player.PLAYER1);
                }else if( game.timerGame.GetTime(TimerGame.Player.PLAYER1).compareTo(game.timerGame.GetTime(TimerGame.Player.PLAYER2)) > 0 ){
                    game.timerGame.winning(TimerGame.Player.PLAYER2);
                }
/*
                int n1 = game.timerGame.GetScore(TimerGame.Player.PLAYER1);
                int n2 = game.timerGame.GetScore(TimerGame.Player.PLAYER2);

                this.sendJsonMessage(game.player1, game,
                        new TextMessage("当前比分 " + n1 + ":" + n2));
                this.sendJsonMessage(game.player2, game,
                        new TextMessage("当前比分 " + n2 + ":" + n1));
                        */
            }

            if (game.timerGame.isOver()) {
                game.player1.close();
                game.player2.close();
            }
        } catch (IOException e) {
            this.handleException(e, game);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("gameId") long gameId) {
        Game game = TimerServer.games.get(gameId);
        if (game == null)
            return;
        boolean isPlayer1 = session == game.player1;
        if (game.timerGame == null) {
            TimerGame.removeQueuedGame(game.gameId);
        } else if (!game.timerGame.isOver()) {
            game.timerGame.forfeit(isPlayer1 ? TimerGame.Player.PLAYER1 :
                    TimerGame.Player.PLAYER2);
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
                    .sendText(TimerServer.mapper.writeValueAsString(message));
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

        public TimerGame timerGame;

        public boolean vsAI = false;
    }

    public static class Move {
        private String time;
        public String getTime() {
            return time;
        }
        public void setTime(String time) {
            this.time = time;
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
        private final TimerGame game;
        private final String scramble;

        public GameStartedMessage(TimerGame game,String scramble) {
            super("gameStarted", "");
            this.game = game;
            this.scramble = scramble;
        }

        public TimerGame getGame() {
            return game;
        }

        public String getScramble(){return scramble;}
    }

    public static class OpponentMadeMoveMessage extends Message {
        private final Move move;
        private final String scramble;

        public OpponentMadeMoveMessage(Move move,String scramble) {
            super("opponentMadeMove", "");
            this.move = move;
            this.scramble = scramble;
        }

        public Move getMove() {
            return move;
        }

        public String getScramble() {
            return scramble;
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

    public static class ResultMessage extends Message {
        private final String r0;
        private final String r1;

        public ResultMessage(String r0, String r1) {
            super("result", "");
            this.r0 = r0;
            this.r1 = r1;
        }

        public String getR0() {
            return r0;
        }

        public String getR1(){
            return r1;
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
