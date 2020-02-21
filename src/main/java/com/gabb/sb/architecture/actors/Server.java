package com.gabb.sb.architecture.actors;


import ch.qos.logback.classic.Level;
import com.gabb.sb.Util;
import com.gabb.sb.architecture.payloads.dispatching.IPayloadDispatcher;
import com.gabb.sb.architecture.resolver.IResolver;
import com.gabb.sb.architecture.factory.UtilFactory;
import com.gabb.sb.architecture.websocket.ServerWebSocketHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
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
public class Server {

	private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
	
	public static final String HOST = "172.16.11.96";
	public static final int PORT = 8080;

	public static void main(String[] args) {
		Util.configureLoggersProgrammatically(Level.INFO);
		Vertx vertx = Vertx.vertx();
		HttpServer server = vertx.createHttpServer();
		IResolver resolver = UtilFactory.testJsonResolver();
		IPayloadDispatcher router = UtilFactory.testDispatcher();
		ServerWebSocketHandler handler = new ServerWebSocketHandler().setResolver(resolver).setDispatcher(router);
		server.websocketHandler(handler).listen(PORT, HOST);
		LOGGER.info("Listening on {}:{}", HOST, server.actualPort());
	}
}

