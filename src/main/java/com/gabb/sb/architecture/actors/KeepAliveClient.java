package com.gabb.sb.architecture.actors;

import com.gabb.sb.architecture.Util;
import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.messages.StartTestMessage;
import com.gabb.sb.architecture.messages.TestRunnerFinished;
import com.gabb.sb.architecture.messages.publish.AbstractMessagePublisher;
import com.gabb.sb.architecture.messages.publish.IMessagePublisher;
import com.gabb.sb.architecture.resolver.IMessageResolver;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class KeepAliveClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeepAliveClient.class);
	
	private final HttpClient oHttpClient;
	private final String oHost;
	private final int oPort;
	private final IMessagePublisher oPublisher;
	private String oUri;
	private WebSocket oSocket;
	private IMessageResolver oResolver;

	public KeepAliveClient(int port, String host) {
		oResolver = Util.testJsonResolver();
		oPublisher = getPublisher();
		oHttpClient = Vertx.vertx().createHttpClient();
		oHttpClient.connectionHandler(this::onHttpConnectionEstablished);
		oPort = port;
		oHost = host;
		oUri = "/";
	}

	private IMessagePublisher getPublisher() {
		return AbstractMessagePublisher.builder().addSubscriber(StartTestMessage.class, stm -> new Thread(() -> {
			LOGGER.info("MOCK: Received Start Test Message {}, {}. mocking 5 second test", stm.buildPath, stm.cucumberArgs);
			try { Thread.sleep(5000); } catch (InterruptedException ignored) { }
			LOGGER.info("MOCK: Sending TestRunnerFinished");
			String result = new Random().nextBoolean() ? "FAIL" : "PASS";
			TestRunnerFinished message = new TestRunnerFinished(result, "server:/home/mms/ftp/yaddayadda");
			oSocket.writeBinaryMessage(oResolver.resolve(message));
		}).start()).build();
	}

	public KeepAliveClient(String aHost, int aPort, String aUri) {
		this(aPort, aHost);
		oUri = aUri;
	}

	private void onHttpConnectionEstablished(HttpConnection aConn){
		LOGGER.info("Http Connection Established {}", aConn);
		//doesn't seem to be called...
		
	}
	
	private void onWebSocketConnected(WebSocket aSocket){
		LOGGER.info("WebSocket Connected {}", aSocket);
		aSocket.closeHandler(__ -> {
			LOGGER.info("WebSocket Closed {}. Reconnecting", aSocket);
			connect();
		});
		
		aSocket.handler(buf -> {
			IMessage resolvedMessage = oResolver.resolve(buf);
			if(resolvedMessage != null){
				oPublisher.publish(resolvedMessage);
			} else {
				LOGGER.info("Recieved Unresolvable Buffer: '{}'", buf.toString());
			}
		});
		aSocket.exceptionHandler(ex -> LOGGER.error("Unhandled exception in socket", ex));
		oSocket = aSocket;
	}
	
	private void onWebSocketFailedToConnect(Throwable aThrowable){
		new Thread(() -> {
			LOGGER.error("WebSocket Failed to Connect: {}. Retrying in 1500ms", aThrowable.getMessage());
			try { Thread.sleep(1500); } catch (InterruptedException aE) { return; }
			connect();
		}).start();
	}
	

	public void connect(){
		oHttpClient.websocket(oPort, oHost, oUri, this::onWebSocketConnected, this::onWebSocketFailedToConnect);
	}

}
