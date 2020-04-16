package com.gabb.sb.runner;

import com.gabb.sb.Status;
import com.gabb.sb.Util;
import com.gabb.sb.events.IEvent;
import com.gabb.sb.events.bus.ConcurrentEventBus;
import com.gabb.sb.events.bus.IEventBus;
import com.gabb.sb.events.concretes.StartRunEvent;
import com.gabb.sb.events.concretes.StopTestEvent;
import com.gabb.sb.events.concretes.TestRunnerFinishedEvent;
import com.gabb.sb.events.resolver.IEventResolver;
import io.vertx.core.Vertx;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Random;

import static com.gabb.sb.Loggers.TEST_RUNNER_APPLICATION_LOGGER;
import static com.gabb.sb.PropertyKeys.*;

@Component
public class TestExecutor {

	private static boolean cSendFinishEventToServer = true;

	@Autowired
	private Environment oEnvironment;

	private final IEventBus oEventBus;
	private final HttpClient oHttpClient;

	private WebSocket oSocket;
	private IEventResolver oResolver;
	private Thread oExecutionThread;
	private boolean oKeepAlive;
	private String oUri;
	private String oHost;
	private int oPort;

	public TestExecutor() {
		oHttpClient = Vertx.vertx().createHttpClient();
		oResolver = Util.testJsonResolver();
		oEventBus = ConcurrentEventBus.builder()
				.addListener(StartRunEvent.class, this::startRun)
				.addListener(StopTestEvent.class, this::stop)
				.build();
	}

	private void startRun(StartRunEvent sre) {
		oExecutionThread = new Thread(() -> {
			String finish = System.getProperty("finish");
			if(!cSendFinishEventToServer) {
				TEST_RUNNER_APPLICATION_LOGGER.info("cSendFinishEventToServer was true, aborting test, not reporting back");
				return;
			}
			try {
				int millis = 5000 + new Random().nextInt(15000);
				TEST_RUNNER_APPLICATION_LOGGER.info("Mocking test for {}ms", millis);
				Thread.sleep(millis);
			}
			catch (InterruptedException ignored) {
				TEST_RUNNER_APPLICATION_LOGGER.info("Execution Thread Interrupted");
				return;
			}
			TEST_RUNNER_APPLICATION_LOGGER.info("Sending TestRunnerFinishedEvent for runId {}", sre.runId);
			Status result = new Random().nextBoolean() ? Status.FAIL : Status.PASS;
			TestRunnerFinishedEvent message =
					new TestRunnerFinishedEvent(result, "server:/home/mms/ftp/yaddayadda", sre.runId);
			try {
				oSocket.writeBinaryMessage(oResolver.resolve(message));
			} catch (Throwable th){
				//can catch here and submit to queue of events to be sent once re-connected
				th.printStackTrace();
			}
		});
		oExecutionThread.start();
	}

	private void stop(StopTestEvent ste) {
		TEST_RUNNER_APPLICATION_LOGGER.info("RECEIVED StopTestEvent; STOPPING TEST");
		oExecutionThread.interrupt();

	}

	private void onWebSocketConnected(WebSocket aSocket){
		TEST_RUNNER_APPLICATION_LOGGER.info("WebSocket Connected {}", aSocket);
		aSocket.closeHandler(__ -> {
			TEST_RUNNER_APPLICATION_LOGGER.info("WebSocket Closed {}", aSocket);
			if(oKeepAlive) {
				TEST_RUNNER_APPLICATION_LOGGER.info("Attempting to reconnect socket...");
				connect();
			}
		});
		aSocket.handler(buf -> {
			IEvent event = oResolver.resolve(buf);
			if(event != null){
				oEventBus.push(event);
			} else {
				TEST_RUNNER_APPLICATION_LOGGER.info("Recieved Unresolvable Buffer: '{}'", buf.toString());
			}
		});
		aSocket.exceptionHandler(ex -> TEST_RUNNER_APPLICATION_LOGGER.error("Unhandled exception in socket. Exception message='{}'", ex.getMessage()));
		oSocket = aSocket;
	}

	private void onWebSocketFailedToConnect(Throwable aThrowable){
		if(oKeepAlive) new Thread(() -> {
			TEST_RUNNER_APPLICATION_LOGGER.error("WebSocket Failed to Connect. Exception message='{}'. Retrying in 1500ms", aThrowable.getMessage());
			try { Thread.sleep(1500); } catch (InterruptedException aE) { return; }
			connect();
		}).start();
	}

	private void connect(){
		var multi = new CaseInsensitiveHeaders();
		multi.add(CONFIG_PORTAL_PORT_KEY, oEnvironment.getProperty("local.server.port"));
		multi.add(BENCH_TAGS_KEY, String.join(BENCH_TAG_DELIMITER, TestRunnerConfig.getInstance().getBenchTags()));
		oHttpClient.websocket(oPort, oHost, oUri, multi, this::onWebSocketConnected, this::onWebSocketFailedToConnect);
	}

	/**
	 * 	starts the websocket, attempting to re-connect upon failure
	 */
	public void startWebSocket(int aServerPort, String aServerHost, String aUri){
		oKeepAlive = true;
		oPort = aServerPort;
		oHost = aServerHost;
		oUri = aUri;
		connect();
	}

	/**
	 * stops the websocket, disabling re-connection feature
	 */
	public void stopWebSocket(){
		oKeepAlive = false;
		if (oExecutionThread != null) oExecutionThread.interrupt();
		if(oSocket != null) oSocket.close();
	}


	public boolean isUp() {
		return oKeepAlive;
	}
}
