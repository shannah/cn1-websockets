/**
 *  Copyright (c) 2015 Steve Hannah
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.codename1.io.websocket;

import com.codename1.system.NativeLookup;
import com.codename1.ui.util.WeakHashMap;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 *
 * @author shannah
 */
public abstract class WebSocket {
    private static int nextId = 1;
    private WebSocketNativeImpl impl;
    
    public static class WebSocketException extends IOException {
        private final int code;
        public WebSocketException(String message, int code) {
            super(message);
            this.code = code;
            
        }
        public int getCode(){
            return code;
        }
    }
    
    private static WeakHashMap<Integer, WebSocket> sockets = new WeakHashMap<Integer, WebSocket>();
    
    // Utility method for the iOS port to create a new byte array
    static byte[] newByteArray(int len) {
        return new byte[len];
    }
    
    static void messageReceived(int id, String message) {
        WebSocket socket = sockets.get(id);
        if (socket == null) {
            sockets.remove(id);
        } else {
            socket.onMessage(message);
        }
    }
    
    static void messageReceived(int id, byte[] message) {
        WebSocket socket = sockets.get(id);
        if (socket == null) {
            sockets.remove(id);
        } else {
            socket.onMessage(message);
        }
    }
    
    static void closeReceived(int id, int statusCode, String reason) {
        WebSocket socket = sockets.get(id);
        if (socket == null) {
            sockets.remove(id);
        } else {
            socket.onClose(statusCode, reason);
            sockets.remove(id);
        }
    }
    
    static void openReceived(int id) {
        WebSocket socket = sockets.get(id);
        if (socket == null) {
            sockets.remove(id);
        } else {
            socket.onOpen();
        }
    }
    
    static void errorReceived(int id, String message, int code) {
        WebSocket socket = sockets.get(id);
        if (socket == null) {
            sockets.remove(id);
        } else {
            WebSocketException ex = new WebSocketException(message, code);
            socket.onError(ex);
        }
    }
    
    public WebSocket(String url) {
        impl = (WebSocketNativeImpl)NativeLookup.create(WebSocketNativeImpl.class);
        impl.setId(nextId++);
        sockets.put(impl.getId(), this);
        impl.setUrl(url);
    }
    
    protected abstract void onOpen();
    protected abstract void onClose(int statusCode, String reason);
    protected abstract void onMessage(String message);
    protected abstract void onMessage(byte[] message);
    protected abstract void onError(Exception ex);
    
    public void send(String message) {
        impl.sendString(message);
    }
    public void send(byte[] message) {
        impl.sendBytes(message);
    }
    
    public void close() {
        impl.close();
    }
    
    public void connect() {
        impl.connect();
    }
}
