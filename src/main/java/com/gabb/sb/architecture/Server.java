package com.gabb.sb.architecture;


import ch.qos.logback.classic.Level;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


/**
 * Writing a web socket
 * https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java
 * https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
 * https://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-17#section-1.3
 * https://en.wikipedia.org/wiki/Augmented_Backus%E2%80%93Naur_form
 * https://chrome.google.com/webstore/detail/smart-websocket-client/omalebghpgejjiaoknljcfmglgbpocdp?utm_source=chrome-app-launcher-info-dialog
 * https://vertx.io/docs/vertx-core/java/#_websockets
 * reactive-streams: http://www.reactive-streams.org/
 */
public class Server {

	private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
	
	public static final String HOST = "localhost";
	public static final int PORT = 8080;
	private static ResourcePool cPool;

	public static void main(String[] args) {
		Util.configureLoggersProgrammatically(Level.INFO);
		Vertx vertx = Vertx.vertx();
		HttpServer server = vertx.createHttpServer();
		cPool = new ResourcePool();
		server.websocketHandler(cPool::add).listen(PORT, HOST);
		LOGGER.info("Listening on {}:{}", HOST, server.actualPort());
		new Thread(()  -> {
			/**
			 * In Reality, this will be done in serial, updating the database
			 * before allocation
			 */
			while(true) try {
				Thread.sleep(500);
				cPool.visit(tr -> {
					if("IDLE".equals(tr.getStatus())) {
						tr.startTest(new Run(new Random().nextInt()));
						return true;
					}
					return false;
				});
			} catch (InterruptedException aE) {
				aE.printStackTrace();
			}
		}).start();
	}
}

