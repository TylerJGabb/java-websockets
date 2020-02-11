package com.gabb.sb;


import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Writing a web socket
 * https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java
 * https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
 * https://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-17#section-1.3
 * https://en.wikipedia.org/wiki/Augmented_Backus%E2%80%93Naur_form
 * https://chrome.google.com/webstore/detail/smart-websocket-client/omalebghpgejjiaoknljcfmglgbpocdp?utm_source=chrome-app-launcher-info-dialog
 * https://vertx.io/docs/vertx-core/java/#_websockets
 */
public class App {

	static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		HttpServerOptions options = new HttpServerOptions().setLogActivity(false);
		HttpServer server = vertx.createHttpServer(options);
		//connections are accepted by default unless a custom handshaker is specified
		server.websocketHandler(serverWebSocket -> {
			PeriodicPinger periodicPinger = new PeriodicPinger(serverWebSocket);
			AtomicInteger pongs = new AtomicInteger(1);
			serverWebSocket.handler(buf -> {
				LOGGER.info("handler received buffer containing '{}'. writing this back into the socket", buf.toString());
				serverWebSocket.writeFinalTextFrame(buf.toString());
			}).pongHandler(pong -> {
				LOGGER.info("received pong");
				serverWebSocket.writeFinalTextFrame("received pongs = " + pongs.getAndIncrement());
			}).closeHandler(closeHandler -> periodicPinger.interrupt())
			.writeFinalTextFrame("I See You!");
			periodicPinger.start();
		}).listen(8080, "172.16.11.139");
	}
}

class PeriodicPinger extends Thread {

	private ServerWebSocket socket;

	private PeriodicPinger() {
	}

	public PeriodicPinger(ServerWebSocket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		App.LOGGER.info("PeriodicPinger started for {}", socket);
		while (!isInterrupted()) try {
			Thread.sleep(5000);
			System.out.println("SENDING PING TO " + socket.path());
			socket.writePing(Buffer.buffer());
		} catch (InterruptedException e) {
			interrupt();
			App.LOGGER.info("PeriodicPinger interrupted for {}", socket);
			return;
		}
		App.LOGGER.info("PeriodicPinger expired for {}", socket);
	}
}
