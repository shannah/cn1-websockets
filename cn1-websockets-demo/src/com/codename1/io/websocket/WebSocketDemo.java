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


import com.codename1.components.SpanLabel;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.IOException;

public class WebSocketDemo {

    private Form current;
    private Resources theme;
    WebSocket sock;
    Container chatContainer;

    public static final String SERVER_URL="ws://translation.weblite.ca:8080/cn1-websockets-demo/chat";
    
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
        
        sock = new WebSocket(SERVER_URL) {

            @Override
            protected void onOpen() {
                System.out.println("In onOpen");
                System.out.println("Ready state: "+sock.getReadyState());
            }

            @Override
            protected void onClose(int statusCode, String reason) {
                System.out.println("Closing: "+sock.getReadyState());
            }

            @Override
            protected void onMessage(final String message) {
                System.out.println("Received message "+message);
                System.out.println("Ready state: "+sock.getReadyState());
                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        if (chatContainer == null) {
                            return;
                        }
                        SpanLabel label = new SpanLabel();
                        label.setText(message);
                        chatContainer.addComponent(label);
                        chatContainer.animateHierarchy(100);
                    }
                    
                });
            }

            @Override
            protected void onError(Exception ex) {
                System.out.println("Ready state: "+sock.getReadyState());
                System.out.println("in onError");
            }

             @Override
             protected void onMessage(byte[] message) {
                 
             }
            
        };
        System.out.println("Sending connect");
        System.out.println("Ready State: "+sock.getReadyState());
        sock.connect();
    }
    
    void showLogin() {
        Form f = new Form("Login");
        f.addComponent(new Label("Name: "));
        final TextField nameField = new TextField();
        f.addComponent(nameField);
        f.addComponent(new Button(new Command("Login"){ 

            @Override
            public void actionPerformed(ActionEvent evt) {
                sock.send(nameField.getText());
                showChat();
            }
               
        }));
        f.show();
    }
    
    void showChat() {
        Form f= new Form("Chat");
        f.setLayout(new BorderLayout());
        
        Container south = new Container();
        final TextField tf = new TextField();
        Button send = new Button(new Command("Send") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                sock.send(tf.getText());
                tf.setText("");
            }
             
        });
        
        chatContainer = new Container();
        chatContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        
        south.addComponent(tf);
        south.addComponent(send);
        f.addComponent(BorderLayout.SOUTH, south);
        f.addComponent(BorderLayout.CENTER, chatContainer);
        f.setFormBottomPaddingEditingMode(true);
        f.show();
        
    }
    
    public void start() {
        showLogin();
        
       
        
        
    }

    public void stop() {
        current = Display.getInstance().getCurrent();
    }
    
    public void destroy() {
    }

}
