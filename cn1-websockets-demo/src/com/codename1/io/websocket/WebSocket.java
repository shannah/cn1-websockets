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

import com.codename1.io.Log;
import com.codename1.system.NativeLookup;
import com.codename1.ui.Display;
import com.codename1.ui.util.WeakHashMap;
import java.io.IOException;


/**
 *
 * @author shannah
 */
public abstract class WebSocket {
    private static int nextId = 1;
    private WebSocketNativeImpl impl;
    private String url;
    private Thread socketThread;
    private boolean connecting;
    
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
    
    /**
     * Checks if websockets are supported on this platform.
     * @return 
     */
    public static boolean isSupported() {
        try {
            WebSocketNativeImpl impl = (WebSocketNativeImpl)NativeLookup.create(WebSocketNativeImpl.class);
            return impl != null && impl.isSupported();
        } catch (Throwable t) {
            return false;
        }
    }
    
    private static WeakHashMap<Integer, WebSocket> sockets = new WeakHashMap<Integer, WebSocket>();
    
    // Utility method for the iOS port to create a new byte array
    static byte[] newByteArray(int len) {
        return new byte[len];
    }
    
    /**
     * @deprecated Internal callback for native implementations.
     * @param id
     * @param message 
     */
    public static void messageReceived(int id, String message) {
        WebSocket socket = sockets.get(id);
        if (socket == null) {
            sockets.remove(id);
        } else {
            socket.connecting = false;
            try {
                socket.onMessage(message);
            } catch (Throwable t) {
                Log.e(t);
                socket.onError(new WebSocketException(t != null ? t.getMessage() : "Unknown Error", 500));
            }
        }
    }
    
    /**
     * @deprecated Internal callback for native implementations
     * @param id
     * @param message 
     */
    public static void messageReceived(int id, byte[] message) {
        WebSocket socket = sockets.get(id);
        if (socket == null) {
            sockets.remove(id);
        } else {
            try {
                socket.onMessage(message);
            } catch (Throwable t) {
                Log.e(t);
                socket.onError(new WebSocketException(t.getMessage(), 500));
            }
        }
    }
    
    /**
     * @deprecated Internal callback for native implementations.  This wraps messageReceived
     * to work arround issue with callbacks in javascript port.
     * @param id
     * @param message 
     */
    public static void messageReceivedBytes(int id, byte[] message) {
        messageReceived(id, message);
    }
    
    /**
     * @deprecated Internal callback for native implementations
     * @param id
     * @param statusCode
     * @param reason 
     */
    public static void closeReceived(int id, int statusCode, String reason) {
        WebSocket socket = sockets.get(id);
        if (socket == null) {
            sockets.remove(id);
        } else {
            try {
                socket.onClose(statusCode, reason);
                sockets.remove(id);
            } catch (Throwable t) {
                Log.e(t);
            }
        }
    }
    
    /**
     * @deprecated Internal callback for native implementations.
     * @param id 
     */
    public static void openReceived(int id) {
        WebSocket socket = sockets.get(id);
        if (socket == null) {
            sockets.remove(id);
        } else {
            try {
                socket.onOpen();
            } catch (Throwable t) {
                Log.e(t);
            }
        }
    }
    
    /**
     * @deprecated Internal callback for native implementations.
     * @param id
     * @param message
     * @param code 
     */
    public static void errorReceived(int id, String message, int code) {
        WebSocket socket = sockets.get(id);
        if (socket == null) {
            if (message == null) {
                message = "null";
            }
            System.out.println("WebSocket error received: ID="+id+", MSG="+message+", code="+code);
            sockets.remove(id);
        } else {
            WebSocketException ex = new WebSocketException(message, code);
            try {
                socket.onError(ex);
            } catch (Throwable t) {
                Log.e(t);
            }
        }
    }
    
    public WebSocket(String url) {
        this.url = url;
        
        impl = (WebSocketNativeImpl)NativeLookup.create(WebSocketNativeImpl.class);
        impl.setId(nextId++);
        sockets.put(impl.getId(), this);
        //impl.setUrl(url);
        //System.out.println("url is set");
    }
    
    protected abstract void onOpen();
    protected abstract void onClose(int statusCode, String reason);
    protected abstract void onMessage(String message);
    protected abstract void onMessage(byte[] message);
    protected abstract void onError(Exception ex);
    
    public void send(String message) {
        if (getReadyState() == WebSocketState.OPEN) {
            impl.sendString(message);
        } else {
            this.onError(new IOException("Attempt to send message while socket is not open. "+getReadyState()));
        }
    }
    public void send(byte[] message) {
        if (getReadyState() == WebSocketState.OPEN) {
            impl.sendBytes(message);
        } else {
            this.onError(new IOException("Attempt to send message while socket is not open. "+getReadyState()));
        }
    }
    
    public void close() {
        if (impl != null && getReadyState() != WebSocketState.CLOSED) {
            impl.close();
        }
    }
    
    public void connect() {
        if (connecting || getReadyState() != WebSocketState.CLOSED) {
            return;
        }
        if (Display.getInstance().isEdt()) {
            socketThread = Display.getInstance().startThread(new Runnable() {
                public void run() {
                    connect();
                }
            }, "WebSocket");
            socketThread.start();
            
        } else {
            connecting = true;
            try {
                impl.setUrl(url);
                impl.connect();
            } finally {
                connecting = false;
            }
            
        }
    }
    
    public WebSocketState getReadyState() {
        if (impl == null) {
            if (connecting) {
                return WebSocketState.CONNECTING;
            }
            return WebSocketState.CLOSED;
        }
        int state = impl.getReadyState();
        switch (state) {
            case 0 : return WebSocketState.CONNECTING;
            case 1 : return WebSocketState.OPEN;
            case 2: return WebSocketState.CLOSING;
            default: return WebSocketState.CLOSED;
        }
    }
}
