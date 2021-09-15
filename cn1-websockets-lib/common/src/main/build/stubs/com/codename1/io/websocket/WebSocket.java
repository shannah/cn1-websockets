package com.codename1.io.websocket;


/**
 * 
 *  @author shannah
 */
public abstract class WebSocket {

	/**
	 *  The WebSocket() constructor returns a new WebSocket object.
	 *  @param url The URL to which to connect; this should be the URL to which the WebSocket server will respond.
	 *  @param protocols Space-delimited string of sub-protocols, so that a single server can implement multiple WebSocket sub-protocols (for example, you might want one server to be able to handle different types of interactions depending on the specified protocol). If you don't specify a protocol string, an empty string is assumed.
	 */
	public WebSocket(String url, String protocols) {
	}

	/**
	 *  The WebSocket() constructor returns a new WebSocket object.
	 *  @param url The URL to which to connect; this should be the URL to which the WebSocket server will respond.
	 */
	public WebSocket(String url) {
	}

	/**
	 *  Checks whether websocket callbacks (e.g. onMessage, onError, onOpen, onClose) should
	 *  be delivered on the EDT.  This is true by default.  Can be disabled using {@link #setCallbacksOnEdt(boolean) }
	 *  @return the callbacksOnEdt
	 *  @see #setCallbacksOnEdt(boolean) 
	 */
	public boolean isCallbacksOnEdt() {
	}

	/**
	 *  Sets whether websocket callbacks (e.g. onMessage, onError, onOpen, onClose) should be delivered
	 *  on the EDT.  This is true by default.
	 *  @param callbacksOnEdt the callbacksOnEdt to set
	 *  @see #isCallbacksOnEdt() 
	 */
	public void setCallbacksOnEdt(boolean callbacksOnEdt) {
	}

	public static void setDebugLoggingEnabled(boolean enabled) {
	}

	public static boolean isDebugLoggingEnabled() {
	}

	/**
	 *  Checks if websockets are supported on this platform.
	 *  @return 
	 */
	public static boolean isSupported() {
	}

	/**
	 *  @deprecated Internal callback for native implementations.
	 *  @param id
	 *  @param message 
	 */
	public static void messageReceived(int id, String message) {
	}

	/**
	 *  @deprecated Internal callback for native implementations
	 *  @param id
	 *  @param message 
	 */
	public static void messageReceived(int id, byte[] message) {
	}

	/**
	 *  @deprecated Internal callback for native implementations.  This wraps messageReceived
	 *  to work arround issue with callbacks in javascript port.
	 *  @param id
	 *  @param message 
	 */
	public static void messageReceivedBytes(int id, byte[] message) {
	}

	/**
	 *  @deprecated Internal callback for native implementations
	 *  @param id
	 *  @param statusCode
	 *  @param reason 
	 */
	public static void closeReceived(int id, int statusCode, String reason) {
	}

	/**
	 *  @deprecated Internal callback for native implementations.
	 *  @param id 
	 */
	public static void openReceived(int id) {
	}

	/**
	 *  @deprecated Internal callback for native implementations.
	 *  @param id
	 *  @param message
	 *  @param code 
	 */
	public static void errorReceived(int id, String message, int code) {
	}

	/**
	 *  @deprecated Internal callback for native implementations.
	 *  @param id
	 *  @param message
	 *  @param code 
	 *  @param cause Exception that caused the error
	 */
	public static void errorReceived(int id, String message, int code, Throwable cause) {
	}

	/**
	 *  Waits until the WebSocket connection is ready.  Since websockets are
	 *  asyncronous, you can't simply start using the socket immediately after
	 *  calling {@link #connect()}, you must wait until the connection is opened
	 *  (i.e. after the onOpen() callback.  This method will block until the 
	 *  state of the socket is no longer {@link WebSocketState#CONNECTING}.
	 *  
	 *  <p>This method is safe for the EDT.  If run on the EDT, it will use {@link CN#invokeAndBlock(java.lang.Runnable) }
	 *  under the hood.  If called from another thread, it will actually block the thread.</p>
	 *  @return {@literal true} if the socket is opened.  {@literal false} if the socket
	 *  is not opened.  It is possible that after waiting the socket has already been closed
	 *  or that the connection failed with an error of some kind.  
	 */
	public boolean waitReady() {
	}

	/**
	 *  Sets the auto reconnect timeout.  If the socket is closed due to an error, it will 
	 *  attempt to automatically reconnect after the given interval.  
	 *  @param timeout The timeout (in milliseconds) to wait after disconnection, before attempting to connect again.
	 *  @return Self for chaining.
	 */
	public WebSocket autoReconnect(long timeout) {
	}

	protected abstract void onOpen() {
	}

	protected abstract void onClose(int statusCode, String reason) {
	}

	protected abstract void onMessage(String message) {
	}

	protected abstract void onMessage(byte[] message) {
	}

	protected abstract void onError(Exception ex) {
	}

	public void send(String message) {
	}

	public void send(byte[] message) {
	}

	public void close() {
	}

	/**
	 *  Tries to reconnect to the webservice after it has been closed.
	 */
	public void reconnect() {
	}

	/**
	 *  Connects to the websocket with a timeout.  If it hasn't connected by the time the timeout
	 *  has elapsed, then a WebSocketException will be posted to the {@link #onError(java.lang.Exception) } method with
	 *  code {@link #ERR_TIMEOUT}, and the connection will be closed.
	 *  @param timeout The timeout in milliseconds.  Values {@literal <= 0} mean infinite timeout.
	 */
	public void connect(int timeout) {
	}

	/**
	 *  A space-delimited string of sub-protocols that a single server can implement multiple WebSocket sub-protocols (for example, you might want one server to be able to handle different types of interactions depending on the specified protocol). If you don't specify a protocol string, an empty string is assumed.
	 *  @param protocols Space-delimited string of sub-protocols.
	 */
	public void setProtocols(String protocols) {
	}

	/**
	 *  Gets the space-delimited string of sub-protocols, or null if none are set.
	 *  @return Space-delimited string or null.
	 */
	public String getProtocols() {
	}

	/**
	 *  Attempts to connect to the webservice.
	 */
	public void connect() {
	}

	public WebSocketState getReadyState() {
	}

	public static class WebSocketException {


		public WebSocketException(String message, int code) {
		}

		public WebSocketException(String message, int code, Throwable cause) {
		}

		public int getCode() {
		}
	}
}
