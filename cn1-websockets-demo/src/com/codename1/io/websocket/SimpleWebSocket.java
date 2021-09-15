/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codename1.io.websocket;

import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shannah
 */
public class SimpleWebSocket extends WebSocket {
    private List<WebSocketListener> listeners = new ArrayList<WebSocketListener>();
    private List<WebSocketListener> tmp = new ArrayList<WebSocketListener>();
    
    
    
    private class OpenAdapter extends WebSocketAdapter {
        private Runnable r;

        private OpenAdapter(Runnable r) {
            this.r = r;
        }
        
        @Override
        public void onOpen() {
            r.run();
        }
    }
    
    public class CloseEvent extends ActionEvent {
        private int code;
        private String reason;
        
        CloseEvent(int code, String reason) {
            super(SimpleWebSocket.this);
            this.code = code;
            this.reason = reason;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getReason() {
            return reason;
        }
        
        public WebSocket getSource() {
            return SimpleWebSocket.this;
        }
    }
    
    private class CloseAdapter extends WebSocketAdapter {
        private ActionListener<CloseEvent> l;

        private CloseAdapter(ActionListener<CloseEvent> l) {
            this.l = l;
        }
        
        @Override
        public void onClose(int statusCode, String reason) {
            l.actionPerformed(new CloseEvent(statusCode, reason));
        }
        
        
    }
    
    public class ErrorEvent extends ActionEvent {
        private Exception ex;
        
        ErrorEvent(Exception ex) {
            super(SimpleWebSocket.this);
            this.ex = ex;
        }
        
        public Exception getError() {
            return ex;
        }
    }

    private class ErrorAdapter extends WebSocketAdapter {
        private ActionListener<ErrorEvent> l;
        
        private ErrorAdapter(ActionListener<ErrorEvent> l) {
            this.l =l;
        }

        @Override
        public void onError(Exception ex) {
            l.actionPerformed(new ErrorEvent(ex));
        }
    }
    
    public class MessageEvent extends ActionEvent {
        private String string;
        private byte[] bytes;
        
        MessageEvent(String message) {
            super(SimpleWebSocket.this);
            this.string = message;
        }
        
        MessageEvent(byte[] message) {
            super(SimpleWebSocket.this);
            this.bytes = message;
        }
        
        public String getString() {
            return string;
        }
        
        public byte[] getBytes() {
            return bytes;
        }
    }
    
    
    private class MessageAdapter extends WebSocketAdapter {
        private ActionListener<MessageEvent> l;
        
        private MessageAdapter(ActionListener<MessageEvent> l) {
            this.l = l;
        }

        @Override
        public void onMessage(String message) {
            l.actionPerformed(new MessageEvent(message));
        }

        @Override
        public void onMessage(byte[] message) {
            l.actionPerformed(new MessageEvent(message));
        }

    }
    
    public SimpleWebSocket(String url, String protocols) {
        super(url, protocols);
    }
    
    public SimpleWebSocket(String url) {
        super(url);
    }
    
    public void addWebSocketListener(WebSocketListener l) {
        listeners.add(l);
    }
    
    public void removeWebSocketListener(WebSocketListener l) {
        listeners.remove(l);
    }
    
    public void addOpenListener(Runnable r) {
        addWebSocketListener(new OpenAdapter(r));
    }
    
    public void removeOpenListener(Runnable r) {
        for (WebSocketListener l : listeners) {
            if (l instanceof OpenAdapter) {
                OpenAdapter a = (OpenAdapter)l;
                if (a.r == r) {
                    removeWebSocketListener(l);
                    return;
                }
            }
        }
    }
    
    
    public void addCloseListener(ActionListener<CloseEvent> l) {
        addWebSocketListener(new CloseAdapter(l));
    }
    
    public void removeCloseListener(ActionListener<CloseEvent> li) {
        for (WebSocketListener l : listeners) {
            if (l instanceof CloseAdapter) {
                CloseAdapter a = (CloseAdapter)l;
                if (a.l == li) {
                    removeWebSocketListener(l);
                    return;
                }
            }
        }
    }
    
    public void addErrorListener(ActionListener<CloseEvent> l) {
        addWebSocketListener(new CloseAdapter(l));
    }
    
    public void removeErrorListener(ActionListener<ErrorEvent> li) {
        for (WebSocketListener l : listeners) {
            if (l instanceof ErrorAdapter) {
                ErrorAdapter a = (ErrorAdapter)l;
                if (a.l == li) {
                    removeWebSocketListener(l);
                    return;
                }
            }
        }
    }
    
    public void addMessageListener(ActionListener<MessageEvent> l) {
        addWebSocketListener(new MessageAdapter(l));
    }
    
     public void removeMessageListener(ActionListener<MessageEvent> li) {
        for (WebSocketListener l : listeners) {
            if (l instanceof MessageAdapter) {
                MessageAdapter a = (MessageAdapter)l;
                if (a.l == li) {
                    removeWebSocketListener(l);
                    return;
                }
            }
        }
    }
    
    
    @Override
    protected void onOpen() {
        if (!listeners.isEmpty()) {
            tmp.clear();
            tmp.addAll(listeners);
            for (WebSocketListener l : tmp) {
                l.onOpen();
            }
        }
    }

    @Override
    protected void onClose(int statusCode, String reason) {
        if (!listeners.isEmpty()) {
            tmp.clear();
            tmp.addAll(listeners);
            for (WebSocketListener l : tmp) {
                l.onClose(statusCode, reason);
            }
        }
    }

    @Override
    protected void onMessage(String message) {
        if (!listeners.isEmpty()) {
            tmp.clear();
            tmp.addAll(listeners);
            for (WebSocketListener l : tmp) {
                l.onMessage(message);
            }
        }
    }

    @Override
    protected void onMessage(byte[] message) {
        if (!listeners.isEmpty()) {
            tmp.clear();
            tmp.addAll(listeners);
            for (WebSocketListener l : tmp) {
                l.onMessage(message);
            }
        }
    }

    @Override
    protected void onError(Exception ex) {
        if (!listeners.isEmpty()) {
            tmp.clear();
            tmp.addAll(listeners);
            for (WebSocketListener l : tmp) {
                l.onError(ex);
            }
        }
    }
    
}
