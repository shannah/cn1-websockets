/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.io.websocket;

import com.codename1.system.NativeInterface;

/**
 *
 * @author shannah
 */
public interface WebSocketNativeImpl extends NativeInterface {
    public void setUrl(String url);
    public void setId(int id);
    public int getId();
    public void sendBytes(byte[] message);
    public void sendString(String message);
    public void close();
    public void connect();
}
