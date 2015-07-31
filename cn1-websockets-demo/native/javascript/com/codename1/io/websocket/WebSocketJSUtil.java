package com.codename1.io.websocket;

import org.teavm.dom.typedarrays.Uint8Array;
/**
 *
 * @author shannah
 */
public class WebSocketJSUtil {
    
    public static byte[] toByteArray(Object o) {
        return toByteArray((Uint8Array)o);
    }
    
    public static byte[] toByteArray(Uint8Array bytes) {
        int len = bytes.getLength();
        byte[] out = new byte[len];
        //Uint8Array arr = ((Window)JS.getGlobal()).createUint8Array(len);
        for (int i=0; i<len; i++){
            out[i] = bytes.get(i);
            //arr.set(i, bytes[i]);
        }
        
        return out;
    }
}
