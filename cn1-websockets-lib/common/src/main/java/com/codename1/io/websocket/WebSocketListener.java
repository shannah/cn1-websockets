/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.io.websocket;

/**
 *
 * @author shannah
 */
public interface WebSocketListener {
    public void onOpen();
    public void onClose(int statusCode, String reason);
    public void onMessage(String message);
    public void onMessage(byte[] message);
    public void onError(Exception ex);
}
