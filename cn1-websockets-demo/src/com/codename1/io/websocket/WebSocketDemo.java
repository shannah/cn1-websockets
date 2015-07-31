package com.codename1.io.websocket;


import com.codename1.components.SpanLabel;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.IOException;

public class WebSocketDemo {

    private Form current;
    private Resources theme;

    public void init(Object context) {
        try {
            theme = Resources.openLayered("/theme");
            UIManager.getInstance().setThemeProps(theme.getTheme(theme.getThemeResourceNames()[0]));
        } catch(IOException e){
            e.printStackTrace();
        }
        // Pro users - uncomment this code to get crash reports sent to you automatically
        /*Display.getInstance().addEdtErrorHandler(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                evt.consume();
                Log.p("Exception in AppName version " + Display.getInstance().getProperty("AppVersion", "Unknown"));
                Log.p("OS " + Display.getInstance().getPlatformName());
                Log.p("Error " + evt.getSource());
                Log.p("Current Form " + Display.getInstance().getCurrent().getName());
                Log.e((Throwable)evt.getSource());
                Log.sendLog();
            }
        });*/
    }
    
    public void start() {
        
         final WebSocket sock = new WebSocket("ws://10.0.1.21:8080/WebSocketServer/whiteboardendpoint") {

            @Override
            protected void onOpen() {
                System.out.println("In onOpen");
            }

            @Override
            protected void onClose(int statusCode, String reason) {
                
            }

            @Override
            protected void onMessage(String message) {
                System.out.println("Received message "+message);
            }

            @Override
            protected void onError(Exception ex) {
                System.out.println("in onError");
            }

             @Override
             protected void onMessage(byte[] message) {
                 
             }
            
        };
        System.out.println("Sending connect");
        sock.connect();
        
        if(current != null){
            current.show();
            return;
        }
        Form hi = new Form("Hi World");
        hi.addComponent(new Label("Hi World"));
        final SpanLabel l = new SpanLabel();
        hi.addComponent(l);
        hi.addComponent(new Button(new Command("Ping Peer") {

             @Override
             public void actionPerformed(ActionEvent evt) {
                 sock.send("Hello From Codename One");
             }

             
        }));
        hi.show();
        
       
        
        
    }

    public void stop() {
        current = Display.getInstance().getCurrent();
    }
    
    public void destroy() {
    }

}
