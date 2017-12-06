/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.acuo.experimental.tyrus;

import com.acuo.experimental.tyrus.chatdata.ChatDecoder;
import com.acuo.experimental.tyrus.chatdata.ChatMessage;
import com.acuo.experimental.tyrus.chatdata.ChatTranscriptUpdateMessage;
import com.acuo.experimental.tyrus.chatdata.ChatUpdateMessage;
import com.acuo.experimental.tyrus.chatdata.DisconnectRequestMessage;
import com.acuo.experimental.tyrus.chatdata.DisconnectResponseEncoder;
import com.acuo.experimental.tyrus.chatdata.DisconnectResponseMessage;
import com.acuo.experimental.tyrus.chatdata.LoginRequestMessage;
import com.acuo.experimental.tyrus.chatdata.LoginResponseMessage;
import com.acuo.experimental.tyrus.chatdata.UserListUpdateMessage;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


@ServerEndpoint(
        value = "/chat",
        decoders = {ChatDecoder.class},
        encoders = {DisconnectResponseEncoder.class}
)
public class ChatEndpoint {

    static final Logger logger = Logger.getLogger("application");

    private static ConcurrentHashMap<String, Session> connections = new ConcurrentHashMap<String, Session>();

    private List<String> chatTranscript = new ArrayList<String>();
    static int transcriptMaxLines = 20;

    @OnOpen
    public void init(Session s) {
        logger.info("############Someone connected...");
    }

    @OnMessage
    public void handleMessage(ChatMessage message, Session session) {
        final String messageType = message.getType();

        if (messageType.equals(ChatMessage.LOGIN_REQUEST)) {
            handleLoginRequest((LoginRequestMessage) message, session);
        } else if (messageType.equals(ChatMessage.CHAT_MESSAGE)) {
            handleChatMessage((ChatUpdateMessage) message);
        } else if (messageType.equals(ChatMessage.DISCONNECT_REQUEST)) {
            handleDisconnectRequest((DisconnectRequestMessage) message);
        }
    }

    public void handleLoginRequest(LoginRequestMessage message, Session session) {
        String newUsername = this.registerNewUsername(message.getUsername(), session);
        logger.info("Signing " + newUsername + " into chat.");
        LoginResponseMessage lres = new LoginResponseMessage(newUsername);
        try {
            session.getBasicRemote().sendText(lres.asString());
        } catch (IOException ioe) {
            logger.warning("Error signing " + message.getUsername() + " into chat : " + ioe.getMessage());
        }

        this.addToTranscriptAndNotify(newUsername, " has just joined.");
        this.broadcastUserList();
    }

    public void handleChatMessage(ChatUpdateMessage message) {
        logger.info("Receiving chat message from " + message.getUsername());
        this.addToTranscriptAndNotify(message.getUsername(), message.getMessage());
    }

    public DisconnectResponseMessage handleDisconnectRequest(DisconnectRequestMessage drm) {
        logger.info(drm.getUsername() + " would like to leave chat");
        DisconnectResponseMessage reply = new DisconnectResponseMessage(drm.getUsername());
        this.addToTranscriptAndNotify(drm.getUsername(), " has just left.");
        this.removeUserAndBroadcast(drm.getUsername());
        return reply;
    }

    @OnClose
    public void handleClientClose(Session session) {
        String username = null;
        logger.info("The web socket closed");
        for (String s : connections.keySet()) {
            if (session.equals(connections.get(s))) {
                username = s;
            }
        }

        if (username != null) {
            this.removeUserAndBroadcast(username);
            this.addToTranscriptAndNotify(username, " has just left...rather abruptly !");
        }
    }

    private void broadcastUserList() {
        logger.info("Broadcasting updated user list");
        UserListUpdateMessage ulum = new UserListUpdateMessage(new ArrayList(connections.keySet()));
        for (Session nextSession : connections.values()) {
            RemoteEndpoint.Basic remote = nextSession.getBasicRemote();
            try {
                remote.sendText(ulum.asString());
            } catch (IOException ioe) {
                logger.warning("Error updating a client " + remote + " : " + ioe.getMessage());
            }
        }
    }

    private void removeUserAndBroadcast(String username) {
        logger.info("Removing " + username + " from chat.");
        Session nextSession = connections.get(username);

        try {
            nextSession.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "User logged off"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        connections.remove(username);
        this.broadcastUserList();
    }

    private void broadcastUpdatedTranscript() {
        List transcriptEntry = new ArrayList();
        transcriptEntry.add(this.chatTranscript.get(this.chatTranscript.size() - 1));
        logger.info("Broadcasting updated transcript with " + transcriptEntry);

        for (Session nextSession : connections.values()) {
            RemoteEndpoint.Basic remote = nextSession.getBasicRemote();
            if (remote != null) {
                ChatTranscriptUpdateMessage cm = new ChatTranscriptUpdateMessage(transcriptEntry);
                try {
                    remote.sendText(cm.asString());
                } catch (IOException ioe) {
                    logger.warning("Error updating a client " + remote + " : " + ioe.getMessage());
                }
            }
        }
    }

    private void addToTranscriptAndNotify(String user, String message) {
        if (chatTranscript.size() > transcriptMaxLines) {
            chatTranscript.remove(0);
        }
        chatTranscript.add(user + "> " + message);
        this.broadcastUpdatedTranscript();
    }

    private String registerNewUsername(String newUsername, Session session) {
        if (connections.containsKey(newUsername)) {
            return this.registerNewUsername(newUsername + "1", session);
        }

        connections.put(newUsername, session);
        return newUsername;
    }
}