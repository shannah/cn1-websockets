/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.cn1.websocket.demo.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author shannah
 */
@ServerEndpoint("/chat")
public class ChatDemoEndpoint {

    
    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());
   @OnMessage
    public String onMessage(Session session, String message) {
        System.out.println(session.getUserProperties());
        if (!session.getUserProperties().containsKey("name")) {
            session.getUserProperties().put("name", message);
            return null;
        }
        for (Session peer: peers) {
            try {
                peer.getBasicRemote().sendText(session.getUserProperties().get("name")+": "+message);
            } catch (IOException ex) {
                Logger.getLogger(ChatDemoEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    @OnOpen
    public void onOpen(Session peer) {
        peers.add(peer);
    }
    
    @OnClose
    public void onClose(Session peer) {
        peers.remove(peer);
    }
    
}
