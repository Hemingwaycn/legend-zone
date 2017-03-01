package com.wrox;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/blackAndWhite/{gameId}/{username}")
public class BlackAndWhiteServer
{
    private static Map<Long, Game> games = new Hashtable<>();
    private static ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") long gameId,
                       @PathParam("username") String username)
    {
        try
        {
            BlackAndWhiteGame blackAndWhiteGame = BlackAndWhiteGame.getActiveGame(gameId);
            if(blackAndWhiteGame != null)
            {
                session.close(new CloseReason(
                        CloseReason.CloseCodes.UNEXPECTED_CONDITION,
                        "This game has already started."
                ));
            }

            List<String> actions = session.getRequestParameterMap().get("action");
            if(actions != null && actions.size() == 1)
            {
                String action = actions.get(0);
                if("start".equalsIgnoreCase(action))
                {
                    Game game = new Game();
                    game.gameId = gameId;
                    game.player1 = session;
                    BlackAndWhiteServer.games.put(gameId, game);
                }
                else if("join".equalsIgnoreCase(action))
                {
                    Game game = BlackAndWhiteServer.games.get(gameId);
                    game.player2 = session;
                    game.blackAndWhiteGame = BlackAndWhiteGame.startGame(gameId, username);
                    this.sendJsonMessage(game.player1, game,
                            new GameStartedMessage(game.blackAndWhiteGame));
                    this.sendJsonMessage(game.player2, game,
                            new GameStartedMessage(game.blackAndWhiteGame));
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            try
            {
                session.close(new CloseReason(
                        CloseReason.CloseCodes.UNEXPECTED_CONDITION, e.toString()
                ));
            }
            catch(IOException ignore) { }
        }
    }

    @OnMessage
    public void onMessage(Session session, String message,
                          @PathParam("gameId") long gameId)
    {
        Game game = BlackAndWhiteServer.games.get(gameId);
        boolean isPlayer1 = session == game.player1;

        try
        {
            Move move = BlackAndWhiteServer.mapper.readValue(message, Move.class);
            game.blackAndWhiteGame.move(
                    isPlayer1 ? BlackAndWhiteGame.Player.PLAYER1 :
                            BlackAndWhiteGame.Player.PLAYER2,
                    move.getRow(),
                    move.getColumn()
            );

            int r1 = game.blackAndWhiteGame.GetRound(BlackAndWhiteGame.Player.PLAYER1);
            int r2 = game.blackAndWhiteGame.GetRound(BlackAndWhiteGame.Player.PLAYER2);

            int R = r1>r2?r1:r2;

            String bw = (move.getColumn()+1)%2 == 0 ? "白":"黑";
            if(isPlayer1){
                System.out.println("P1 :" + (move.getColumn()+1) );

                this.sendJsonMessage(game.player1, game,
                        new TextMessage("round" + R + ": 你出的牌是" + (move.getColumn()+1)));
                this.sendJsonMessage(game.player2, game,
                        new TextMessage("round" + R + ": 对手出了一张" + bw + "牌"));
            }else{
                System.out.println("P2 :" + (move.getColumn()+1) );

                this.sendJsonMessage(game.player2, game,
                        new TextMessage("round" + R + ": 你出的牌是" + (move.getColumn()+1)));
                this.sendJsonMessage(game.player1, game,
                        new TextMessage("round" + R + ": 对手出了一张" + bw + "牌"));
            }




            if( r1 == r2 ){
                int winner = 0;
                int n1 = game.blackAndWhiteGame.GetMove(BlackAndWhiteGame.Player.PLAYER1,r1);
                int n2 = game.blackAndWhiteGame.GetMove(BlackAndWhiteGame.Player.PLAYER2,r2);
                if(n1>n2){
                    System.out.println("P1 win this round." );
                    this.sendJsonMessage(game.player1, game,
                            new TextMessage("round" + R + ": 本回合胜利."));
                    this.sendJsonMessage(game.player2, game,
                            new TextMessage("round" + R + ": 本回合失败."));

                    this.sendJsonMessage(game.player1, game,
                            new OpponentMadeMoveMessage(move));
                }else if(n1<n2){
                    System.out.println("P2 win this round." );
                    this.sendJsonMessage(game.player2, game,
                            new TextMessage("round" + R + ": 本回合胜利."));
                    this.sendJsonMessage(game.player1, game,
                            new TextMessage("round" + R + ": 本回合失败."));

                    this.sendJsonMessage(game.player2, game,
                            new OpponentMadeMoveMessage(move));
                }else{
                    this.sendJsonMessage(game.player2, game,
                            new TextMessage("round" + R + ": 本回合平局."));
                    this.sendJsonMessage(game.player1, game,
                            new TextMessage("round" + R + ": 本回合平局."));
                    System.out.println("draw." );

                    this.sendJsonMessage((isPlayer1 ? game.player1 : game.player2), game,
                            new OpponentMadeMoveMessage(move));
                }
            }else{
                this.sendJsonMessage((isPlayer1 ? game.player2 : game.player1), game,
                        new OpponentMadeMoveMessage(move));
            }


            if(game.blackAndWhiteGame.isOver())
            {
                if(game.blackAndWhiteGame.isDraw())
                {
                    this.sendJsonMessage(game.player1, game,
                            new GameIsDrawMessage());
                    this.sendJsonMessage(game.player2, game,
                            new GameIsDrawMessage());
                }
                else
                {
                    boolean wasPlayer1 = game.blackAndWhiteGame.getWinner() ==
                            BlackAndWhiteGame.Player.PLAYER1;
                    this.sendJsonMessage(game.player1, game,
                            new GameOverMessage(wasPlayer1));
                    this.sendJsonMessage(game.player2, game,
                            new GameOverMessage(!wasPlayer1));
                }
                game.player1.close();
                game.player2.close();
            }
        }
        catch(IOException e)
        {
            this.handleException(e, game);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("gameId") long gameId)
    {
        Game game = BlackAndWhiteServer.games.get(gameId);
        if(game == null)
            return;
        boolean isPlayer1 = session == game.player1;
        if(game.blackAndWhiteGame == null)
        {
            BlackAndWhiteGame.removeQueuedGame(game.gameId);
        }
        else if(!game.blackAndWhiteGame.isOver())
        {
            game.blackAndWhiteGame.forfeit(isPlayer1 ? BlackAndWhiteGame.Player.PLAYER1 :
                    BlackAndWhiteGame.Player.PLAYER2);
            Session opponent = (isPlayer1 ? game.player2 : game.player1);
            this.sendJsonMessage(opponent, game, new GameForfeitedMessage());
            try
            {
                opponent.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void sendJsonMessage(Session session, Game game, Message message)
    {
        try
        {
            session.getBasicRemote()
                   .sendText(BlackAndWhiteServer.mapper.writeValueAsString(message));
        }
        catch(IOException e)
        {
            this.handleException(e, game);
        }
    }

    private void handleException(Throwable t, Game game)
    {
        t.printStackTrace();
        String message = t.toString();
        try
        {
            game.player1.close(new CloseReason(
                    CloseReason.CloseCodes.UNEXPECTED_CONDITION, message
            ));
        }
        catch(IOException ignore) { }
        try
        {
            game.player2.close(new CloseReason(
                    CloseReason.CloseCodes.UNEXPECTED_CONDITION, message
            ));
        }
        catch(IOException ignore) { }
    }

    private static class Game
    {
        public long gameId;

        public Session player1;

        public Session player2;

        public BlackAndWhiteGame blackAndWhiteGame;
    }

    public static class Move
    {
        private int row;

        private int column;

        public int getRow()
        {
            return row;
        }

        public void setRow(int row)
        {
            this.row = row;
        }

        public int getColumn()
        {
            return column;
        }

        public void setColumn(int column)
        {
            this.column = column;
        }
    }

    public static abstract class Message
    {
        private final String action;
        private final String msg;
        public Message(String action,String msg)
        {
            this.action = action;
            this.msg = msg;
        }

        public String getAction()
        {
            return this.action;
        }
        public String getMsg()
        {
            return this.msg;
        }
    }

    public static class GameStartedMessage extends Message
    {
        private final BlackAndWhiteGame game;

        public GameStartedMessage(BlackAndWhiteGame game)
        {
            super("gameStarted","");
            this.game = game;
        }

        public BlackAndWhiteGame getGame()
        {
            return game;
        }
    }

    public static class OpponentMadeMoveMessage extends Message
    {
        private final Move move;

        public OpponentMadeMoveMessage(Move move)
        {
            super("opponentMadeMove","");
            this.move = move;
        }

        public Move getMove()
        {
            return move;
        }
    }

    public static class GameOverMessage extends Message
    {
        private final boolean winner;

        public GameOverMessage(boolean winner)
        {
            super("gameOver","");
            this.winner = winner;
        }

        public boolean isWinner()
        {
            return winner;
        }
    }

    public static class GameIsDrawMessage extends Message
    {
        public GameIsDrawMessage()
        {
            super("gameIsDraw","");
        }
    }

    public static class GameForfeitedMessage extends Message
    {
        public GameForfeitedMessage()
        {
            super("gameForfeited","");
        }
    }

    public static class TextMessage extends Message
    {
        public TextMessage(String msg)
        {
            super("text",msg);
        }
    }
}
