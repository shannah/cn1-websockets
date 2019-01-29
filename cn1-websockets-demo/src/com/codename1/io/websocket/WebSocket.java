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
import com.codename1.io.Util;
import com.codename1.system.NativeLookup;
import com.codename1.ui.CN;
import com.codename1.ui.Display;
import com.codename1.ui.util.WeakHashMap;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


/**
 *
 * @author shannah
 */
public abstract class WebSocket {
    private static int nextId = 1;
    private static final int ERR_TIMEOUT = 999;
    private WebSocketNativeImpl impl;
    private String url;
    private Thread socketThread;
    private boolean connecting;
    private long autoReconnectTimeout;
    private Timer reconnectTimer, connectTimer;
    private Thread reconnectTimerThread;
    
    public static class WebSocketException extends IOException {
        private final int code;
        public WebSocketException(String message, int code) {
            super(message);
            this.code = code;
            
        }
        
        public WebSocketException(String message, int code, Throwable cause) {
            super(message, cause);
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
        final WebSocket socket = sockets.get(id);
        if (socket == null) {
            sockets.remove(id);
        } else {
            try {
                socket.onClose(statusCode, reason);
                sockets.remove(id);
                if (socket.reconnectTimer == null && statusCode != 1000 && socket.autoReconnectTimeout > 0) {
                    socket.initReconnect();
                }
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
            if (socket.reconnectTimer != null) {
                socket.reconnectTimer.cancel();
                socket.reconnectTimer = null;
            }
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
        errorReceived(id, message, code, null);
    }
    
    /**
     * @deprecated Internal callback for native implementations.
     * @param id
     * @param message
     * @param code 
     * @param cause Exception that caused the error
     */
    public static void errorReceived(int id, String message, int code, Throwable cause) {
        WebSocket socket = sockets.get(id);
        if (socket == null) {
            if (message == null) {
                message = "null";
            }
            System.out.println("WebSocket error received: ID="+id+", MSG="+message+", code="+code);
            sockets.remove(id);
        } else {
            WebSocketException ex = new WebSocketException(message, code, cause);
            try {
                socket.onError(ex);
            } catch (Throwable t) {
                Log.e(t);
            }
        }
    }
    
    /**
     * Waits until the WebSocket connection is ready.  Since websockets are
     * asyncronous, you can't simply start using the socket immediately after
     * calling {@link #connect()}, you must wait until the connection is opened
     * (i.e. after the onOpen() callback.  This method will block until the 
     * state of the socket is no longer {@link WebSocketState#CONNECTING}.
     * 
     * <p>This method is safe for the EDT.  If run on the EDT, it will use {@link CN#invokeAndBlock(java.lang.Runnable) }
     * under the hood.  If called from another thread, it will actually block the thread.</p>
     * @return {@literal true} if the socket is opened.  {@literal false} if the socket
     * is not opened.  It is possible that after waiting the socket has already been closed
     * or that the connection failed with an error of some kind.  
     */
    public boolean waitReady() {
        if (CN.isEdt()) {
            final boolean[] res = new boolean[1];
            CN.invokeAndBlock(new Runnable() {
                public void run() {
                    res[0] = waitReady();
                }
            });
            return res[0];
        }
        
        while (getReadyState() == WebSocketState.CONNECTING) {
            Util.sleep(100);   
        }
        return getReadyState() == WebSocketState.OPEN;
    }
    
    /**
     * Sets the auto reconnect timeout.  If the socket is closed due to an error, it will 
     * attempt to automatically reconnect after the given interval.  
     * @param timeout The timeout (in milliseconds) to wait after disconnection, before attempting to connect again.
     * @return Self for chaining.
     */
    public WebSocket autoReconnect(long timeout) {
        autoReconnectTimeout = timeout;
        return this;
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
        // https://github.com/shannah/cn1-websockets/issues/14
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }
        if (connectTimer != null) {
            connectTimer.cancel();
            connectTimer = null;
        }
        if (impl != null && getReadyState() != WebSocketState.CLOSED) {
            impl.close();
        }
        connecting = false;
    }

    private void initReconnect() {
        if (reconnectTimer == null && autoReconnectTimeout > 0) {
            reconnectTimer = new Timer();
            reconnectTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    reconnectTimerThread = Thread.currentThread();
                    reconnect();
                }

            }, autoReconnectTimeout, autoReconnectTimeout);
        }
    }
    
    /**
     * Tries to reconnect to the webservice after it has been closed.
     */
    public void reconnect() {
        if (connecting || getReadyState() != WebSocketState.CLOSED) {
            return;
        }
        System.out.println("Attempting to reconnect...");
        impl = (WebSocketNativeImpl)NativeLookup.create(WebSocketNativeImpl.class);
        impl.setId(nextId++);
        sockets.put(impl.getId(), this);
        connecting = false;
        connectHasBeenCalledAtLeastOnce=false;
        try {
            connect();
        } catch (Throwable t) {
            System.out.println("Failed to reconnect.  Will make another attempt in "+autoReconnectTimeout+"ms");
        }
    }
    
    /**
     * Connects to the websocket with a timeout.  If it hasn't connected by the time the timeout
     * has elapsed, then a WebSocketException will be posted to the {@link #onError(java.lang.Exception) } method with
     * code {@link #ERR_TIMEOUT}, and the connection will be closed.
     * @param timeout The timeout in milliseconds.  Values {@literal <= 0} mean infinite timeout.
     */
    public void connect(int timeout) {
        if (timeout > 0) {
            connectTimer = new Timer();
            connectTimer.schedule(new TimerTask() {
                public void run() {
                    connectTimer = null;
                    if (connecting) {
                        try {
                            onError(new WebSocketException("WebSocket connect timeout", ERR_TIMEOUT));
                            close();
                        } catch (Throwable t) {
                            Log.e(t);
                        }
                    }
                }
            }, timeout);
        } 
        connect();
        
    }
    
    /**
     * Attempts to connect to the webservice.
     */
    public void connect() {
        connect(true);
    }
    
    private boolean connectHasBeenCalledAtLeastOnce;
    private void connect(boolean throwErrorIfNotFirstConnectAttempt) {
        try {
            if( throwErrorIfNotFirstConnectAttempt) {
                
                if (connectHasBeenCalledAtLeastOnce) {
                    throw new IllegalStateException("WebSocket.connect() can only be called once.");
                }
                connectHasBeenCalledAtLeastOnce = true;
               
            }
            if (connecting || getReadyState() != WebSocketState.CLOSED) {
                return;
            }
            if (Display.getInstance().isEdt()) {
                socketThread = Display.getInstance().startThread(new Runnable() {
                    public void run() {
                        connect(false);
                    }
                }, "WebSocket");
                socketThread.start();

            } else {
                connecting = true;
                // If autoreconnect is on, then we'll enable it during connection
                // to cover the case where it doesn't successfully connect.
                initReconnect();
                try {
                    impl.setUrl(url);
                    impl.connect();
                } finally {
                    connecting = false;
                    if (connectTimer != null) {
                        connectTimer.cancel();
                        connectTimer = null;
                    }
                }

            }
        } catch (Throwable t) {
            onError(new WebSocketException("Exception occurred while trying to connect.", 500, t));
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
