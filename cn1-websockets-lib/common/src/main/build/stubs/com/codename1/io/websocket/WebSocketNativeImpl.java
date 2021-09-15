package com.codename1.io.websocket;


/**
 * 
 *  @author shannah
 */
public interface WebSocketNativeImpl {

	public String getProtocols();

	public void setProtocols(String protocols);

	public void setUrl(String url);

	public void setId(int id);

	public int getId();

	public void sendBytes(byte[] message);

	public void sendString(String message);

	public void close();

	public void connect();

	public int getReadyState();
}
