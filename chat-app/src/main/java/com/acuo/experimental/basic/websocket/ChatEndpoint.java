package com.acuo.experimental.basic.websocket;

import com.acuo.experimental.basic.model.Message;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/chat/{username}", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class ChatEndpoint {

    private static ConcurrentHashMap<String, Session> connections = new ConcurrentHashMap<>();
    private static HashMap<String, String> users = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {

        registerNewUsername(username, session);
        users.put(session.getId(), username);

        Message message = new Message();
        message.setFrom(username);
        message.setContent("Connected!");
        broadcast(message);
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        message.setFrom(users.get(session.getId()));
        broadcast(message);
    }

    @OnClose
    public void onClose(Session session) {
        String username = null;

        for (String s : connections.keySet()) {
            if (session.equals(connections.get(s))) {
                username = s;
            }
        }

        if (username != null) {
            this.removeUserAndBroadcast(username);
        }

        Message message = new Message();
        message.setFrom(users.get(session.getId()));
        message.setContent("Disconnected!");
        broadcast(message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

    private static void broadcast(Message message) {
        connections.values().forEach(session -> {
            synchronized (session) {
                try {
                    final RemoteEndpoint.Basic remote = session.getBasicRemote();
                    if (remote != null)
                        remote.sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void removeUserAndBroadcast(String username) {
        Session nextSession = connections.get(username);

        try {
            nextSession.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "User logged off"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        connections.remove(username);
    }

    private String registerNewUsername(String newUsername, Session session) {
        if (connections.containsKey(newUsername)) {
            return this.registerNewUsername(newUsername + "1", session);
        }

        connections.put(newUsername, session);
        return newUsername;
    }

}
