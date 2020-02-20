package com.gabb.sb;


import ch.qos.logback.classic.Level;
import com.gabb.sb.architecture.FooBar;
import com.gabb.sb.architecture.IPayload;
import com.gabb.sb.architecture.IResolver;
import com.gabb.sb.architecture.JsonResolver;
import com.gabb.sb.architecture.Message;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


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
	public static final String HOST = "172.16.11.96";
	public static final int PORT = 8080;

	public static void main(String[] args) {
		configureLoggersProgrammatically(Level.INFO);
		startWebSocketServer();
	}

	public static void configureLoggersProgrammatically(Level aLevel) {
		List<String> loggersNamedByPackage = Arrays.asList(
				"io.netty.handler.codec.http.websocketx",
				"io.netty.buffer",
				"io.netty.util.internal",
				"io.netty.util",
				"io.netty.channel",
				"io.netty.resolver.dns"
		);

		for (String mPackage : loggersNamedByPackage) {
			((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(mPackage)).setLevel(aLevel);
		}
	}

	private static void startWebSocketServer() {
		Vertx vertx = Vertx.vertx();
		HttpServer server = vertx.createHttpServer();
		IResolver resolver = buildResolver();
		//connections are accepted by default unless a custom handshaker is specified
		server.websocketHandler(serverWebSocket -> {
			KeepAlive keepAlive = new KeepAlive(serverWebSocket).handleOverduePong(s -> {
				s.close();
				return true; //stop the keepalive
			});
			serverWebSocket.handler(buf -> {
				if (buf.length() == 0) {
					LOGGER.trace("received empty payload from {}, ignoring...", serverWebSocket.remoteAddress());
					return;
				}

				IPayload payload = resolver.resolve(buf);
				if(payload == null){
					LOGGER.error("Unable to resolve payload " + buf);
				} else {
					LOGGER.info("Resolved payload " + payload.getClass());
				}
				
				LOGGER.info("handler received buffer containing '{}'", buf.toString());
			}).closeHandler(closeHandler -> keepAlive.interrupt())
					.writeFinalTextFrame("I See You!");
			keepAlive.start();
		}).listen(PORT, HOST);
		LOGGER.info("Listening on {}:{}", HOST, server.actualPort());
	}

	private static IResolver buildResolver() {
		IResolver resolver = new JsonResolver();
		resolver.registerTypeCode(Message.class, 0x01);
		resolver.registerTypeCode(FooBar.class, 0x02);
		return resolver;
	}

}

