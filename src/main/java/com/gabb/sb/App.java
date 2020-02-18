package com.gabb.sb;


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
		startWebSocketServer();
	}

	private static void startWebSocketServer() {
		Vertx vertx = Vertx.vertx();
		HttpServerOptions options = new HttpServerOptions().setLogActivity(false);
		HttpServer server = vertx.createHttpServer(options);
		//connections are accepted by default unless a custom handshaker is specified
		server.websocketHandler(serverWebSocket -> {
			KeepAlive keepAlive = new KeepAlive(serverWebSocket, 1000).handleMissedPong(ServerWebSocket::close);
			serverWebSocket.handler(buf -> {
				LOGGER.info("handler received buffer containing '{}'. writing this back into the socket", buf.toString());
				serverWebSocket.writeFinalTextFrame(buf.toString());
			}).closeHandler(closeHandler -> keepAlive.interrupt())
			.writeFinalTextFrame("I See You!");
			keepAlive.start();
		}).listen(8080, "172.16.11.96");
		LOGGER.info("Listening on ...");
	}
}

