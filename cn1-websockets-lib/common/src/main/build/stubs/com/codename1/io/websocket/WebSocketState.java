package com.codename1.io.websocket;


/**
 * 
 *  @author shannah
 */
public final class WebSocketState extends Enum {

	/**
	 *  An <a href="https://tools.ietf.org/html/rfc6455#section-4">opening
	 *  handshake</a> is being performed.
	 */
	public static final WebSocketState CONNECTING;

	/**
	 *  The WebSocket connection is established (= the <a href=
	 *  "https://tools.ietf.org/html/rfc6455#section-4">opening handshake</a>
	 *  has succeeded) and usable.
	 */
	public static final WebSocketState OPEN;

	/**
	 *  A <a href="https://tools.ietf.org/html/rfc6455#section-7">closing
	 *  handshake</a> is being performed.
	 */
	public static final WebSocketState CLOSING;

	/**
	 *  The WebSocket connection is closed.
	 */
	public static final WebSocketState CLOSED;

	public static WebSocketState[] values() {
	}

	public static WebSocketState valueOf(String name) {
	}
}
