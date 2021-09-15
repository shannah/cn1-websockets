package com.codename1.io.websocket;


/**
 * 
 *  @author shannah
 */
public interface WebSocketListener {

	public void onOpen();

	public void onClose(int statusCode, String reason);

	public void onMessage(String message);

	public void onMessage(byte[] message);

	public void onError(Exception ex);
}
