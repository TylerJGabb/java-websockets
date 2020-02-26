package com.gabb.sb.architecture.actors;

import ch.qos.logback.classic.Level;
import com.gabb.sb.Util;
import com.gabb.sb.architecture.factory.UtilFactory;
import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.messages.dispatching.IMessageDispatcher;
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
	private IMessageDispatcher oIMessageDispatcher;
	private WebSocket oSocket;

	public void setResolver(IMessageResolver aResolver) {
		oIMessageResolver = aResolver;
	}

	public void setoIMessageDispatcher(IMessageDispatcher aDispatcher) {
		oIMessageDispatcher = aDispatcher;
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
		aSocket.closeHandler(__ -> {
			LOGGER.info("WebSocket Closed {}. Reconnecting", aSocket);
			connect();
		});
		aSocket.handler(buf -> {
			IMessage resolvedMessage = oIMessageResolver.resolve(buf);
			if(resolvedMessage != null){
				oIMessageDispatcher.dispatch(resolvedMessage);
			} else {
				LOGGER.info("Recieved Unresolvable Buffer: '{}'", buf.toString());
			}
		}).writeTextMessage("Connected!");
		aSocket.exceptionHandler(ex -> LOGGER.error("Unhandled exception in socket", ex));
		oSocket = aSocket;
	}
	
	void onWebSocketFailedToConnect(Throwable aThrowable){
		LOGGER.error("WebSocket Failed to Connect: {}. Retrying", aThrowable.getMessage());
		connect();
	}
	

	public void connect(){
		oHttpClient.websocket(oPort, oHost, oUri, this::onWebSocketConnected, this::onWebSocketFailedToConnect);
	}

	/**
	 * TODO: GET RID OF THIS!!!
	 */
	public static void main(String[] args) {
		Util.configureLoggersProgrammatically(Level.OFF);
		KeepAliveClient client = new KeepAliveClient(Server.PORT, Server.HOST);
		client.setResolver(UtilFactory.testJsonResolver());
		client.setoIMessageDispatcher(UtilFactory.testDispatcher());
		client.connect();
	}
}
