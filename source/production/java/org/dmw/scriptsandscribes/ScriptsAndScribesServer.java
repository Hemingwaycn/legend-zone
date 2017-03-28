package org.dmw.scriptsandscribes;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.dmw.scriptsandscribes.*;

@ServerEndpoint("/scriptsAndScribes/{gameId}/{username}")
public class ScriptsAndScribesServer {
    private static Map<Long, Game> games = new Hashtable<>();
    private static ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") long gameId,
                       @PathParam("username") String username) {
        try {
            ScriptsAndScribesGame scriptsAndScribesGame = ScriptsAndScribesGame.getActiveGame(gameId);
            if (scriptsAndScribesGame != null) {
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
                    ScriptsAndScribesServer.games.put(gameId, game);
                } else if ("join".equalsIgnoreCase(action)) {
                    Game game = ScriptsAndScribesServer.games.get(gameId);
                    game.player2 = session;
                    game.scriptsAndScribesGame = ScriptsAndScribesGame.startGame(gameId, username);
                    this.sendJsonMessage(game.player1, game,
                            new GameStartedMessage(game.scriptsAndScribesGame));
                    this.sendJsonMessage(game.player2, game,
                            new GameStartedMessage(game.scriptsAndScribesGame));
                } else if ("vsAI".equalsIgnoreCase(action)) {
                    Game game = new Game();
                    game.vsAI = true;
                    game.gameId = gameId;
                    game.player1 = session;
                    game.scriptsAndScribesGame = ScriptsAndScribesGame.startGame(gameId, "Legend's Bot");
                    ScriptsAndScribesServer.games.put(gameId, game);
                    this.sendJsonMessage(game.player1, game,
                            new GameStartedMessage(game.scriptsAndScribesGame));
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
        Game game = ScriptsAndScribesServer.games.get(gameId);
        boolean isPlayer1 = session == game.player1;

        try {
            Move move = ScriptsAndScribesServer.mapper.readValue(message, Move.class);
            game.scriptsAndScribesGame.move(
                    isPlayer1 ? ScriptsAndScribesGame.Player.PLAYER1 :
                            ScriptsAndScribesGame.Player.PLAYER2,
                    move.getRow(),
                    move.getColumn()
            );

            int r1 = game.scriptsAndScribesGame.GetRound(ScriptsAndScribesGame.Player.PLAYER1);
            int r2 = game.scriptsAndScribesGame.GetRound(ScriptsAndScribesGame.Player.PLAYER2);

            int R = r1 > r2 ? r1 : r2;


            if (isPlayer1) {
                this.sendJsonMessage(game.player1, game,
                        new TextMessage("round" + R + ": 你已经出牌."));


                if (game.vsAI) {
                    int rnd = (new Random()).nextInt(9) + 1;
                    while (true) {
                        boolean found = false;
                        for (int i = 0; i < r2; i++) {
                            if (game.scriptsAndScribesGame.GetMove(ScriptsAndScribesGame.Player.PLAYER2, i + 1) == rnd) {
                                found = true;
                            }
                        }
                        if (found) {
                            rnd = (new Random()).nextInt(9) + 1;
                        } else {
                            break;
                        }
                    }

                    game.scriptsAndScribesGame.move(
                            ScriptsAndScribesGame.Player.PLAYER2,
                            0,
                            rnd - 1
                    );
                    this.sendJsonMessage(game.player1, game,
                            new TextMessage("round" + R + ": 对手已经出牌."));
                    r2++;
                }


                this.sendJsonMessage(game.player2, game,
                        new TextMessage("round" + R + ": 对手已经出牌."));
            } else {
                this.sendJsonMessage(game.player2, game,
                        new TextMessage("round" + R + ": 你已经出牌."));
                this.sendJsonMessage(game.player1, game,
                        new TextMessage("round" + R + ": 对手已经出牌."));
            }


            if (r1 == r2) {
                int n1 = game.scriptsAndScribesGame.GetMove(ScriptsAndScribesGame.Player.PLAYER1, r1);
                int n2 = game.scriptsAndScribesGame.GetMove(ScriptsAndScribesGame.Player.PLAYER2, r2);
                if (n1 > n2) {
                    this.sendJsonMessage(game.player1, game,
                            new TextMessage("round" + R + ": 本回合胜利." + "(" + n1 + ":" + n2 + ")"));
                    this.sendJsonMessage(game.player2, game,
                            new TextMessage("round" + R + ": 本回合失败." + "(" + n2 + ":" + n1 + ")"));
                } else if (n1 < n2) {
                    this.sendJsonMessage(game.player2, game,
                            new TextMessage("round" + R + ": 本回合胜利." + "(" + n2 + ":" + n1 + ")"));
                    this.sendJsonMessage(game.player1, game,
                            new TextMessage("round" + R + ": 本回合失败." + "(" + n1 + ":" + n2 + ")"));
                } else {
                    this.sendJsonMessage(game.player2, game,
                            new TextMessage("round" + R + ": 本回合平局." + "(" + n2 + ":" + n1 + ")"));
                    this.sendJsonMessage(game.player1, game,
                            new TextMessage("round" + R + ": 本回合平局." + "(" + n1 + ":" + n2 + ")"));
                }

                this.sendJsonMessage(game.player1, game,
                        new OpponentMadeMoveMessage(move));
                this.sendJsonMessage(game.player2, game,
                        new OpponentMadeMoveMessage(move));

            } else {

            }


            if (game.scriptsAndScribesGame.isOver()) {
                int s1 = 0;
                int s2 = 0;
                int d = 0;
                for(int i=1;i<=9;i++){
                    int n1 = game.scriptsAndScribesGame.GetMove(ScriptsAndScribesGame.Player.PLAYER1,i);
                    int n2 = game.scriptsAndScribesGame.GetMove(ScriptsAndScribesGame.Player.PLAYER2,i);
                    if(n1 < n2){
                        s2++;
                    }else if(n1 == n2){
                        d++;
                    }else if(n1 > n2){
                        s1++;
                    }
                }

                if (game.scriptsAndScribesGame.isDraw()) {
                    this.sendJsonMessage(game.player1, game,
                            new TextMessage("游戏结束,平局. 总分"+s1+":"+s2));
                    this.sendJsonMessage(game.player2, game,
                            new TextMessage("游戏结束,平局. 总分"+s2+":"+s1));
                    this.sendJsonMessage(game.player1, game,
                            new GameIsDrawMessage());
                    this.sendJsonMessage(game.player2, game,
                            new GameIsDrawMessage());

                } else {
                    boolean wasPlayer1 = game.scriptsAndScribesGame.getWinner() ==
                            ScriptsAndScribesGame.Player.PLAYER1;
                    if (wasPlayer1) {
                        this.sendJsonMessage(game.player1, game,
                                new TextMessage("游戏结束,胜利. 总分"+s1+":"+s2));
                        this.sendJsonMessage(game.player2, game,
                                new TextMessage("游戏结束,失败. 总分"+s2+":"+s1));
                    } else {
                        this.sendJsonMessage(game.player1, game,
                                new TextMessage("游戏结束,失败. 总分"+s1+":"+s2));
                        this.sendJsonMessage(game.player2, game,
                                new TextMessage("游戏结束,胜利. 总分"+s2+":"+s1));
                    }



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
        Game game = ScriptsAndScribesServer.games.get(gameId);
        if (game == null)
            return;
        boolean isPlayer1 = session == game.player1;
        if (game.scriptsAndScribesGame == null) {
            ScriptsAndScribesGame.removeQueuedGame(game.gameId);
        } else if (!game.scriptsAndScribesGame.isOver()) {
            game.scriptsAndScribesGame.forfeit(isPlayer1 ? ScriptsAndScribesGame.Player.PLAYER1 :
                    ScriptsAndScribesGame.Player.PLAYER2);
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
                    .sendText(ScriptsAndScribesServer.mapper.writeValueAsString(message));
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

        public ScriptsAndScribesGame scriptsAndScribesGame;

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
        private final ScriptsAndScribesGame game;

        public GameStartedMessage(ScriptsAndScribesGame game) {
            super("gameStarted", "");
            this.game = game;
        }

        public ScriptsAndScribesGame getGame() {
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
