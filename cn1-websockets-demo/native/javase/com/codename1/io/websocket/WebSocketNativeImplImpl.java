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

//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ServerHandshake;
import static com.neovisionaries.ws.client.WebSocketState.CLOSED;
import static com.neovisionaries.ws.client.WebSocketState.CLOSING;
import static com.neovisionaries.ws.client.WebSocketState.CONNECTING;
import static com.neovisionaries.ws.client.WebSocketState.CREATED;
import static com.neovisionaries.ws.client.WebSocketState.OPEN;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.codename1.io.Log;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class WebSocketNativeImplImpl implements com.codename1.io.websocket.WebSocketNativeImpl {

    //WebSocketClient client;
    com.neovisionaries.ws.client.WebSocket client;
    int id;

    public void close() {
        if (client == null) {
            return;
        }
        client.sendClose();
        
        if (client.getState() == CONNECTING) {
            try {
                client.disconnect();
            } catch (Throwable t){}
            try {
                client.getSocket().close();
            
            } catch (Throwable t){}
            
            
        }
    }

    public void sendBytes(byte[] message) {
        client.sendBinary(message);
    }

    public void sendString(String message) {
        client.sendText(message);
    }

    public void setUrl(String url) {/*
         System.out.println("Setting url to "+url);
         try {
         client = new WebSocketClient(new URI(url)) {
         public void onOpen(ServerHandshake sh) {
         System.out.println("in native onOpen");
         WebSocket.openReceived(id);
         }
                
         public void onMessage(String string) {
         WebSocket.messageReceived(id, string);
         }
                
         public void onClose(int i, String string, boolean bln) {
         WebSocket.closeReceived(id, i, string);
         }
                
         public void onError(Exception excptn) {
         System.out.println("In native onError");
         WebSocket.errorReceived(id, excptn.getMessage(), 500);
         }
         };
         } catch (URISyntaxException ex) {
         throw new RuntimeException(ex);
         }
         */

        try {
            client = new WebSocketFactory().createSocket(url);
            client.addListener(new WebSocketAdapter() {
                @Override
                public void onConnected(com.neovisionaries.ws.client.WebSocket websocket, Map<String, List<String>> headers) {
                    try {
                        WebSocket.openReceived(id);
                    } catch (Throwable t) {
                        try {
                            WebSocket.errorReceived(id, t.getMessage(), 0, t);
                        } catch (Throwable t2) {
                            Log.e(t2);
                        }
                    }
                }

                @Override
                public void onTextMessage(com.neovisionaries.ws.client.WebSocket websocket, String text) {
                    try {
                        WebSocket.messageReceived(id, text);
                    } catch (Throwable t) {
                        try {
                            WebSocket.errorReceived(id, t.getMessage(), 0, t);
                        } catch (Throwable t2) {
                            Log.e(t2);
                        }
                    }
                }

                @Override
                public void onBinaryMessage(com.neovisionaries.ws.client.WebSocket websocket, byte[] binary) {
                    try {
                        WebSocket.messageReceived(id, binary);
                    } catch (Throwable t) {
                        try {
                            WebSocket.errorReceived(id, t.getMessage(), 0, t);
                        } catch (Throwable t2) {
                            Log.e(t2);
                        }
                    }
                }

                @Override
                public void onError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause) {
                    try {
                        WebSocket.errorReceived(id, cause.getMessage(), cause.getError().ordinal(), cause);
                    } catch (Throwable t) {
                        Log.e(t);
                    }
                }

                @Override
                public void onDisconnected(com.neovisionaries.ws.client.WebSocket websocket,
                        WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                        boolean closedByServer) {
                    
                    
                    try {
                        if (closedByServer) {
                            WebSocket.closeReceived(id, serverCloseFrame.getCloseCode(), serverCloseFrame.getCloseReason());
                        } else {
                            WebSocket.closeReceived(id, clientCloseFrame.getCloseCode(), clientCloseFrame.getCloseReason());
                        }
                    } catch (Throwable t) {
                        try {
                            WebSocket.errorReceived(id, t.getMessage(), 0, t);
                        } catch (Throwable t2) {
                            Log.e(t2);
                        }
                    }
                }
            });
        } catch (Exception ex) {
            WebSocket.errorReceived(id, ex.getMessage(), 500, ex);
        }
    }

    public boolean isSupported() {
        return true;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void connect() {
        try {
            client.connect();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
    
    public int getReadyState() {
        if (client == null) {
            return 3;
        }
        switch (client.getState()) {
            case CONNECTING: return 0;
            case OPEN: return 1;
            case CLOSING: return 2;
            default: return 3;
        }
    }

}
