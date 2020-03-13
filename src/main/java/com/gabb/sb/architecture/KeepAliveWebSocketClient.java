package com.gabb.sb.architecture;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;

public class KeepAliveWebSocketClient {


	private final HttpClient oHttpClient;
	private final String oHost;
	private final int oPort;
	private String oUri;
	private WebSocket oSocket;
	private Handler<WebSocket> oOnConnected;

	public KeepAliveWebSocketClient(int port, String host) {
		oHttpClient = Vertx.vertx().createHttpClient();
		oPort = port;
		oHost = host;
		oUri = "/";
	}

	public KeepAliveWebSocketClient(String aHost, int aPort, String aUri) {
		this(aPort, aHost);
		oUri = aUri;
	}

	public void connect(Handler<WebSocket> onConnected){
		oHttpClient.websocket(oPort, oHost, oUri, this::onWebSocketConnected, this::onWebSocketFailedToConnect);
		oOnConnected = onConnected;
	}

	private void onWebSocketFailedToConnect(Throwable aThrowable) {
		
	}

	private void onWebSocketConnected(WebSocket aWebSocket) {
		oSocket = aWebSocket;
		oOnConnected.handle(aWebSocket);
	}

}
