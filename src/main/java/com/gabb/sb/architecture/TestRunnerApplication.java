package com.gabb.sb.architecture;

import ch.qos.logback.classic.Level;
import com.gabb.sb.architecture.events.IEvent;
import com.gabb.sb.architecture.events.bus.ConcurrentEventBus;
import com.gabb.sb.architecture.events.bus.IEventBus;
import com.gabb.sb.architecture.events.concretes.StartRunEvent;
import com.gabb.sb.architecture.events.concretes.StopTestEvent;
import com.gabb.sb.architecture.events.concretes.TestRunnerFinishedEvent;
import com.gabb.sb.architecture.events.resolver.IEventResolver;
import com.gabb.sb.spring.ServerSpringBootApplication;
import io.vertx.core.Vertx;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.WebSocket;

import java.io.IOException;
import java.util.Random;

import static com.gabb.sb.Loggers.TEST_RUNNER_APPLICATION_LOGGER;
import static com.gabb.sb.PropertyKeys.BENCH_TAGS_KEY;
import static com.gabb.sb.PropertyKeys.HUMAN_READABLE_NAME_KEY;
import static com.gabb.sb.spring.ServerSpringBootApplication.HOST;
import static com.gabb.sb.spring.ServerSpringBootApplication.PORT;

public class TestRunnerApplication {

	private static boolean cSendFinishEventToServer;

	private final int oPort;
	private final String oHost;
	private final String oBenchTags;
	private final IEventBus oEventBus;
	private final HttpClient oHttpClient;

	private WebSocket oSocket;
	private IEventResolver oResolver;
	private Thread executionThread;
	private String oUri;
	private String oHumanReadableName;

	public static void main(String[] args) throws IOException {
		loadProperties();
		Util.configureLoggersProgrammatically(Level.INFO);
		new TestRunnerApplication(PORT, HOST).connect();
	}

	private static void loadProperties() throws IOException {
		var sysProps = System.getProperties();
		var runnerProps = TestRunnerApplication.class.getClassLoader().getResourceAsStream("testrunner.properties");
		if(runnerProps != null) sysProps.load(runnerProps);
		String finish = System.getProperty("finish");
		cSendFinishEventToServer = finish != null && Boolean.parseBoolean(finish);
		TEST_RUNNER_APPLICATION_LOGGER.info(cSendFinishEventToServer
				? "PROPERTY FILE SPECIFIED RESPONSE, WILL RESPOND"
				: "PROPERTY FILE SPECIFIED NO RESPONSE, WILL NOT RESPOND");
	}

	public TestRunnerApplication(int port, String host) {
		oResolver = Util.testJsonResolver();
		oEventBus = ConcurrentEventBus.builder()
				.addListener(StartRunEvent.class, this::startRun)
				.addListener(StopTestEvent.class, this::stop)
				.build();
		oHttpClient = Vertx.vertx().createHttpClient();
		oHttpClient.connectionHandler(this::onHttpConnectionEstablished);
		oBenchTags = System.getProperty(BENCH_TAGS_KEY);
		oHumanReadableName = System.getProperty(HUMAN_READABLE_NAME_KEY);
		oPort = port;
		oHost = host;
		oUri = "/";
	}

	public TestRunnerApplication(String aHost, int aPort, String aUri) {
		this(aPort, aHost);
		oUri = aUri;
	}

	private void startRun(StartRunEvent sre) {
		executionThread = new Thread(() -> {
			String finish = System.getProperty("finish");
			if(!cSendFinishEventToServer) {
				TEST_RUNNER_APPLICATION_LOGGER.info("cSendFinishEventToServer was true, aborting test, not reporting back");
				return;
			}
			try {
				int millis = 5000 + new Random().nextInt(15000);
				TEST_RUNNER_APPLICATION_LOGGER.info("Mocking test for {}ms", millis);
				Thread.sleep(millis); } catch (InterruptedException ignored) { return; }
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
		executionThread.start();
	}

	private void stop(StopTestEvent ste) {
		TEST_RUNNER_APPLICATION_LOGGER.info("RECEIVED StopTestEvent; STOPPING TEST");
		executionThread.interrupt();

	}

	private void onHttpConnectionEstablished(HttpConnection aConn){
		TEST_RUNNER_APPLICATION_LOGGER.info("Http Connection Established {}", aConn);
		//doesn't seem to be called...

	}

	private void onWebSocketConnected(WebSocket aSocket){
		TEST_RUNNER_APPLICATION_LOGGER.info("WebSocket Connected {}", aSocket);
		aSocket.closeHandler(__ -> {
			TEST_RUNNER_APPLICATION_LOGGER.info("WebSocket Closed {}. Reconnecting", aSocket);
			connect();
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
		new Thread(() -> {
			TEST_RUNNER_APPLICATION_LOGGER.error("WebSocket Failed to Connect. Exception message='{}'. Retrying in 1500ms", aThrowable.getMessage());
			try { Thread.sleep(1500); } catch (InterruptedException aE) { return; }
			connect();
		}).start();
	}

	public void connect(){
		var multi = new CaseInsensitiveHeaders();
		if(oBenchTags != null) multi.add(BENCH_TAGS_KEY, oBenchTags);
		if(oHumanReadableName != null) multi.add(HUMAN_READABLE_NAME_KEY, oHumanReadableName);
		oHttpClient.websocket(oPort, oHost, oUri, multi, this::onWebSocketConnected, this::onWebSocketFailedToConnect);
	}
}
