package onyx.network;

import onyx.game.Board;
import onyx.game.Coord;
import onyx.game.StoneColor;
import onyx.Main;
import org.javatuples.Triplet;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/room/{id}")
public class RoomEndpoint {
    /**
     * String : room id
     * Session 1 : Black
     * Session 2 : White
     */
    private static Set<Triplet<String, Session, Session>> rooms = new CopyOnWriteArraySet<>();

    /**
     * String : room id
     * Board : current onyx.game
     */
    private static HashMap<String, Board> games = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("id") String roomId) throws IOException {
        Optional<Triplet<String, Session, Session>> optRoom  = findTupleById(roomId);

        if(optRoom.isPresent()) {
            Triplet<String, Session, Session> room = optRoom.get();
            if(room.getValue2() != null) {
                sendTextToSession(session, OTPCommand.ERR_ROOM_FULL);
            }
            else {
                Triplet<String, Session, Session> fullRoom = room.setAt2(session);
                rooms.remove(room);
                rooms.add(fullRoom);
                sendTextToSession(session, OTPCommand.INFO_ROOM_JOINED);
                broadcast(fullRoom, OTPCommand.COMMAND_START);

                sendTextToSession(fullRoom.getValue1(), OTPCommand.COMMAND_READY);
                sendTextToSession(fullRoom.getValue2(), OTPCommand.COMMAND_AWAITING);
            }
        }
        else {
            if(roomId.length() == 6) {
                Triplet<String, Session, Session> newRoom = new Triplet<>(roomId, session, null);
                rooms.add(newRoom);
                games.put(roomId, new Board(6));
                sendTextToSession(session, OTPCommand.INFO_ROOM_CREATED);
                sendTextToSession(session, OTPCommand.COMMAND_AWAITING);
            }
            else if(roomId.length() == 8 && roomId.substring(0, 2).equals("ia")) {
                Triplet<String, Session, Session> newRoom = new Triplet<>(roomId, session, null);
                rooms.add(newRoom);
                games.put(roomId, new Board(6));
                sendTextToSession(session, OTPCommand.INFO_ROOM_CREATED);
                sendTextToSession(session, OTPCommand.COMMAND_AWAITING);
                new Thread(() -> {
                    ProcessBuilder pb = new ProcessBuilder("python3", "./onyx/ai_player.py", roomId);
                    pb.directory(new File(Main.AI_PATH));
                    File log = new File(Main.AI_PATH + "onyx/gamelog/" + roomId + ".log");
                    pb.redirectErrorStream(true);
                    pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
                    try {
                        Process p = pb.start();
                        assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
                        assert pb.redirectOutput().file() == log;
                        assert p.getInputStream().read() == -1;
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }).start();
            }
            else {
                sendTextToSession(session, "!Bad format for Room ID");
            }
        }
    }

    @OnMessage
    public void onMessage(Session session, String received, @PathParam("id") String roomId) throws IOException {
        if(!findTupleBySession(session).isPresent()) return;
        Triplet<String, Session, Session> room  = findTupleBySession(session).get();
        if(room.getValue1() == null || room.getValue2() == null) {
            broadcast(room, OTPCommand.COMMAND_AWAITING);
            return;
        }

        Board board = games.get(roomId);
        if(board == null) {
            sendTextToSession(session, OTPCommand.err("Room does not exist"));
            return;
        }

        if(received.equals("$AVAILABLE")) {
            StringBuilder av = new StringBuilder();
            for(Coord a : board.getAllAvailable()) {
                av.append(a.toString()).append(" ");
            }
            sendTextToSession(session, OTPCommand.info("AVAILABLE " + av));
            return;
        }

        try {
            StoneColor color = session.equals(room.getValue1()) ? StoneColor.BLACK : StoneColor.WHITE;
            Coord c = new Coord(board, received);
            List<Coord> captured = board.addStone(c, color);

            games.put(roomId, board);
            rooms.remove(room);
            rooms.add(room);

            StringBuilder res = new StringBuilder();
            for(Coord cap : captured) {
                res.append(cap.toString()).append(" ");
            }
            broadcast(room, OTPCommand.result(res.toString()));

            if(session.equals(room.getValue1())) {
                sendTextToSession(room.getValue2(), OTPCommand.info("OPPONENT "+ received));
                if(board.isFinished()) {
                    closeIfFinished(board, room);
                }
                else {
                    sendTextToSession(room.getValue1(), OTPCommand.COMMAND_AWAITING);
                    sendTextToSession(room.getValue2(), OTPCommand.COMMAND_READY);
                }
            }
            else {
                sendTextToSession(room.getValue1(), OTPCommand.info("OPPONENT "+ received));
                if(board.isFinished()) {
                    closeIfFinished(board, room);
                }
                else {
                    sendTextToSession(room.getValue1(), OTPCommand.COMMAND_READY);
                    sendTextToSession(room.getValue2(), OTPCommand.COMMAND_AWAITING);
                }
            }

        } catch (Exception e) {
            sendTextToSession(session, OTPCommand.err(e.getMessage()));
        }
    }

    private void closeIfFinished(Board board, Triplet<String, Session, Session> room) throws IOException {
        if(board.getWinner() == StoneColor.BLANK) {
            sendTextToSession(room.getValue1(), OTPCommand.COMMAND_DRAW);
            sendTextToSession(room.getValue2(), OTPCommand.COMMAND_DRAW);
        }
        else if(board.getWinner() == StoneColor.BLACK) {
            sendTextToSession(room.getValue1(), OTPCommand.COMMAND_WIN);
            sendTextToSession(room.getValue2(), OTPCommand.COMMAND_LOOSE);
        }
        else {
            sendTextToSession(room.getValue1(), OTPCommand.COMMAND_LOOSE);
            sendTextToSession(room.getValue2(), OTPCommand.COMMAND_WIN);
        }

        room.getValue1().close();
        room.getValue2().close();
        games.remove(room.getValue0());
        rooms.remove(room);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        if(!findTupleBySession(session).isPresent()) return;
        Triplet<String, Session, Session> room = findTupleBySession(session).get();
        broadcast(room, OTPCommand.COMMAND_END);
        if(room.getValue1() != null) room.getValue1().close();
        if(room.getValue2() != null) room.getValue2().close();
        games.remove(room.getValue0());
        rooms.remove(room);
    }

    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        throwable.printStackTrace();
        sendTextToSession(session, OTPCommand.err(throwable.getMessage()));
    }

    private static Optional<Triplet<String, Session, Session>> findTupleById(String roomId) {
        return rooms
                .stream()
                .filter(tuple -> tuple.getValue0().equals(roomId))
                .findAny();
    }

    private static Optional<Triplet<String, Session, Session>> findTupleBySession(Session session) {
        return rooms.stream()
                .filter(tuple -> (tuple.getValue1() != null && tuple.getValue1().equals(session)) || (tuple.getValue2() != null && tuple.getValue2().equals(session)))
                .findAny();
    }

    private static void broadcast(Triplet<String, Session, Session> room, String message) throws IOException {
        sendTextToSession(room.getValue1(), message);
        sendTextToSession(room.getValue2(), message);
    }

    private static void sendTextToSession(Session session, String message) throws IOException {
        try {
            if(session != null && session.isOpen()) session.getBasicRemote().sendText(message);
        } catch (IllegalStateException ignored) {
        }
    }

}
