package com.gabb.sb.architecture.websocket;

import com.gabb.sb.architecture.connection_integrity.KeepAlive;
import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.messages.publish.IMessagePublisher;
import com.gabb.sb.architecture.resolver.IMessageResolver;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerWebSocketHandler implements Handler<ServerWebSocket> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerWebSocketHandler.class);
	private IMessageResolver oResolver;
	private IMessagePublisher messagePublisher;

	public ServerWebSocketHandler setResolver(IMessageResolver aResolver) {
		oResolver = aResolver;
		return this;
	}

	public ServerWebSocketHandler setPublisher(IMessagePublisher aRouter) {
		messagePublisher = aRouter;
		return this;
	}

	@Override
	public void handle(ServerWebSocket serverWebSocket) {
		//remember that connections are accepted by default unless a custom handshake is specified

		KeepAlive keepAlive = new KeepAlive(serverWebSocket).handleOverduePong(s -> {
			s.close();
			return true; //stop the keepalive
		});
		serverWebSocket.handler(buf -> {
			try {
				if (buf.length() == 0) {
					LOGGER.trace("received empty payload from {}, ignoring...", serverWebSocket.remoteAddress());
				} else {
					IMessage payload = oResolver.resolve(buf);
					if (payload == null) {
						LOGGER.warn("Unable to resolve payload " + buf);
					} else {
						LOGGER.info("Routing payload " + payload.getClass());
						messagePublisher.publish(payload);
					}
				}
			} catch (Exception ex) {
				LOGGER.error("Exception while handling WebSocket Buffer", ex);
			}
		}).closeHandler(closeHandler -> {
			LOGGER.warn("Connection Closed for {}", serverWebSocket.remoteAddress());
			keepAlive.interrupt();
		})
		.exceptionHandler(ex -> LOGGER.error("Unhandled exception in socket", ex))
		.writeFinalTextFrame("Connected!");
		keepAlive.start();
	}
}
