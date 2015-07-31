package com.codename1.io.websocket;

//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ServerHandshake;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class WebSocketNativeImplImpl implements com.codename1.io.websocket.WebSocketNativeImpl {

    //WebSocketClient client;
    com.neovisionaries.ws.client.WebSocket client;
    int id;

    public void close() {
        //client.close();
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
                    WebSocket.openReceived(id);
                }

                @Override
                public void onTextMessage(com.neovisionaries.ws.client.WebSocket websocket, String text) {
                    WebSocket.messageReceived(id, text);
                }

                @Override
                public void onBinaryMessage(com.neovisionaries.ws.client.WebSocket websocket, byte[] binary) {
                    WebSocket.messageReceived(id, binary);
                }

                @Override
                public void onError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause) {
                    WebSocket.errorReceived(id, cause.getMessage(), cause.getError().ordinal());
                }

                @Override
                public void onDisconnected(com.neovisionaries.ws.client.WebSocket websocket,
                        WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                        boolean closedByServer) {
                    if (closedByServer) {
                        WebSocket.closeReceived(id, serverCloseFrame.getCloseCode(), serverCloseFrame.getCloseReason());
                    } else {
                        WebSocket.closeReceived(id, clientCloseFrame.getCloseCode(), clientCloseFrame.getCloseReason());
                    }
                }
            });
        } catch (Exception ex) {
            WebSocket.errorReceived(id, ex.getMessage(), 500);
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

}
