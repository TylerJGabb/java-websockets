package com.gabb.sb.architecture.actors;

import ch.qos.logback.classic.Level;
import com.gabb.sb.Util;
import com.gabb.sb.architecture.resolver.IMessageResolver;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeepAliveClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeepAliveClient.class);
	
	private final HttpClient oHttpClient;
	private final String oHost;
	private final int oPort;
	private String oUri;
	private IMessageResolver oIMessageResolver;
	
	public void setResolver(IMessageResolver aResolver) {
		oIMessageResolver = aResolver;
	}
	
	public KeepAliveClient(int port, String host) {
		oHttpClient = Vertx.vertx().createHttpClient();
		oHttpClient.connectionHandler(this::onHttpConnectionEstablished);
		oPort = port;
		oHost = host;
		oUri = "/";
	}

	public KeepAliveClient(String aHost, int aPort, String aUri) {
		this(aPort, aHost);
		oUri = aUri;
	}

	void onHttpConnectionEstablished(HttpConnection aConn){
		LOGGER.info("Http Connection Established {}", aConn);
		//doesn't seem to be called...
		
	}
	
	void onWebSocketConnected(WebSocket aSocket){
		LOGGER.info("WebSocket Connected {}", aSocket);
	}
	
	void onWebSocketFailedToConnect(Throwable aThrowable){
		LOGGER.error("WebSocket Failed to Connect: {}", aThrowable.getMessage());
	}
	

	public void connect(){
		oHttpClient.websocket(oPort, oHost, oUri, this::onWebSocketConnected, this::onWebSocketFailedToConnect);
	}

	/**
	 * GET RID OF THIS!!!
	 */
	public static void main(String[] args) {
		Util.configureLoggersProgrammatically(Level.OFF);
		KeepAliveClient client = new KeepAliveClient(Server.PORT, Server.HOST);
		client.connect();
	}
}
