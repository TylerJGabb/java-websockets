package com.gabb.sb.architecture;

import com.gabb.sb.architecture.events.bus.ConcurrentEventBus;
import com.gabb.sb.architecture.events.IEvent;
import com.gabb.sb.architecture.events.bus.IEventBus;
import com.gabb.sb.architecture.events.concretes.StartRunEvent;
import com.gabb.sb.architecture.events.concretes.TestRunnerFinishedEvent;
import com.gabb.sb.architecture.events.resolver.IEventResolver;
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
	private final IEventBus oEventBus;
	private String oUri;
	private WebSocket oSocket;
	private IEventResolver oResolver;

	public KeepAliveClient(int port, String host) {
		oResolver = Util.testJsonResolver();
		oEventBus = getEventBus();
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

	private void startRun(StartRunEvent sre) {
		new Thread(() -> {
			LOGGER.info("MOCK: Starting test for run  {}. mocking 5 second test", sre.runId);
			try { Thread.sleep(5000); } catch (InterruptedException ignored) { }
			LOGGER.info("MOCK: Sending TestRunnerFinishedEvent for runId {}", sre.runId);
			String result = new Random().nextBoolean() ? "FAIL" : "PASS";
			TestRunnerFinishedEvent message =
					new TestRunnerFinishedEvent(result, "server:/home/mms/ftp/yaddayadda", sre.runId);
			try {
				oSocket.writeBinaryMessage(oResolver.resolve(message));
			} catch (Throwable th){
				//can catch here and submit to queue of events to be sent once re-connected
				th.printStackTrace();
			}
		}).start();
	}
	
	private IEventBus getEventBus() {
		return ConcurrentEventBus.builder().addListener(StartRunEvent.class, this::startRun).build();
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
			IEvent event = oResolver.resolve(buf);
			if(event != null){
				oEventBus.push(event);
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
