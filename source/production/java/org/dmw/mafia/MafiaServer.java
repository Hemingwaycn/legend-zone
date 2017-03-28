package org.dmw.mafia;

import com.fasterxml.jackson.databind.ObjectMapper;


import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

@ServerEndpoint("/mafia/{gameId}/{username}")
public class MafiaServer{
    private static Map<Long, Game> games = new Hashtable<>();
    private static ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") long gameId,
                       @PathParam("username") String username) {
        try {

            MafiaGame mafiaGame = MafiaGame.getActiveGame(gameId);

            if (mafiaGame != null) {
                session.close(new CloseReason(
                        CloseReason.CloseCodes.UNEXPECTED_CONDITION,
                        "This game has already started."
                ));
            }

            List<String> actions = session.getRequestParameterMap().get("action");
            if (actions != null && actions.size() == 1) {
                String action = actions.get(0);
                if ("create".equalsIgnoreCase(action)) {
                    Game game = new Game();
                    game.gameId = gameId;
                    game.sessions.add(session);
                    Player p = new Player();
                    p.name = username;
                    game.map.put(session,p);
                    MafiaServer.games.put(gameId, game);

                    for (Session s : game.sessions) {

                        this.sendJsonMessage(s, game, new PlayerJoinMessage(username));
                    }



                } else if ("join".equalsIgnoreCase(action)) {
                    Game game = MafiaServer.games.get(gameId);

                    for (Player p : game.map.values()) {
                        this.sendJsonMessage(session, game, new PlayerJoinMessage(p.name));
                    }

                    game.sessions.add(session);
                    Player p = new Player();
                    p.name = username;
                    game.map.put(session,p);

                    for (Session s : game.sessions) {
                        this.sendJsonMessage(s, game, new PlayerJoinMessage(username));
                    }
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
        Game game = MafiaServer.games.get(gameId);

        String pName = game.map.get(session).name;
        try{
            ChatMsg chatMsg = MafiaServer.mapper.readValue(message,ChatMsg.class);

            if(chatMsg.getMsg().charAt(0)=='-'){
                String msgStr = chatMsg.getMsg();
                if(msgStr.equals("-start")){
                    //改变状态
                    if(game.isStarted){
                        return;
                    }else{
                        game.isStarted = true;
                    }

                    //不再显示游戏桌
                    MafiaGame.removeGame(gameId);

                    //发送游戏开始信息
                    for (Session s : game.sessions) {
                        this.sendJsonMessage(s, game,
                                new TextMessage(pName + " 开始了游戏." ));
                    }
                    for (Session s : game.sessions) {
                        this.sendJsonMessage(s, game,
                                new GameStartedMessage(game.mafiaGame));
                    }

                    //分配角色
                    for (Session s : game.sessions) {
                        String roles[] = {"教父","党徒","警长","市民"};
                        Random rnd = new Random();
                        String r = roles[((rnd.nextInt()%4)+4)%4];

                        this.sendJsonMessage(s, game,
                                new AssignRoleMessage(r));
                    }

                    //开始昼夜循环
                    MafiaServer server = this;
                    game.timer.schedule(new TimerTask() {
                        public void run() {
                            if(!game.day){
                                game.dayCount++;
                            }
                            game.day = !game.day;

                            String str = game.day?"白天 ":"夜晚 ";
                            str += game.dayCount;

                            for(Session s : game.sessions){
                                server.sendJsonMessage(s, game,
                                        new TextMessage( str ));
                            }
                        }
                    }, 0, 60000);

                }else if(msgStr.equals("-v")){
                    this.sendJsonMessage(session, game,
                            new TextMessage("你使用了技能." ));
                }
                return;
            }

            for (Session s : game.sessions) {
                this.sendJsonMessage(s, game,
                        new TextMessage(pName + ": " + chatMsg.getMsg()));
            }
        }catch (IOException e){

        }
    }


    @OnClose
    public void onClose(Session session, @PathParam("gameId") long gameId) {
        Game game = MafiaServer.games.get(gameId);
        if (game == null)
            return;

        String pName = game.map.get(session).name;
        game.map.remove(session);
        game.sessions.remove(session);

        // bug
        if(game.sessions.size() == 0){
            MafiaGame.removeGame(gameId);
        }

        for (Session s : game.sessions) {
            this.sendJsonMessage(s, game,
                    new PlayerExitMessage(pName));
        }

    }

    private void sendJsonMessage(Session session, Game game, Message message) {
        if (session == null) {
            return;
        }
        try {
            session.getBasicRemote()
                    .sendText(MafiaServer.mapper.writeValueAsString(message));
        } catch (IOException e) {
            this.handleException(e, game);
        }
    }

    private void handleException(Throwable t, Game game) {
        t.printStackTrace();
        String message = t.toString();
        try {
            for (Session session : game.sessions) {
                session.close(new CloseReason(
                        CloseReason.CloseCodes.UNEXPECTED_CONDITION, message
                ));
            }
        } catch (IOException ignore) {

        }
    }

    private class Game {
        public long gameId;
        public Timer timer;
        int dayCount = 0;
        boolean day = false;
        boolean isStarted = false;

        public Game() {
            sessions = new ArrayList<>();
            map = new HashMap<>();
            timer = new Timer();
        }

        public List<Session> sessions;
        public HashMap<Session,Player> map;



        public MafiaGame mafiaGame;



    }




    private static class Player{
        int id;
        String name;
        public Player(){

        }
    }

    public static class ChatMsg {
        private String msg;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
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
        private final MafiaGame game;

        public GameStartedMessage(MafiaGame game) {
            super("gameStarted", "");
            this.game = game;
        }

        public MafiaGame getGame() {
            return game;
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

    public static class PlayerJoinMessage extends Message {
        private final String name;
        public PlayerJoinMessage(String name) {
            super("playerJoin", name);
            this.name = name;
        }
    }

    public static class PlayerExitMessage extends Message {
        private final String name;
        public PlayerExitMessage(String name) {
            super("playerExit", name);
            this.name = name;
        }
    }

    public static class AssignRoleMessage extends Message {
        private final String role;
        public AssignRoleMessage(String role) {
            super("assignRole", role);
            this.role = role;
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

