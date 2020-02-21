package com.gabb.sb.architecture.actors;

import ch.qos.logback.classic.Level;
import com.gabb.sb.Util;
import com.gabb.sb.architecture.factory.UtilFactory;
import com.gabb.sb.architecture.payloads.PayloadWithInteger;
import com.gabb.sb.architecture.payloads.PayloadWithString;
import com.gabb.sb.architecture.resolver.IResolver;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {


	private static IResolver cResolver;
	private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
	private static HttpClient cClient;

	public static void main(String[] args) {
		Util.configureLoggersProgrammatically(Level.INFO);
		cResolver = UtilFactory.testJsonResolver();
		cClient = Vertx.vertx().createHttpClient();
		cClient.websocket(Server.PORT, Server.HOST, "/", Client::onConnected, Client::onFailureToConnect);
	}

	private static void onFailureToConnect(Throwable aThrowable){
		LOGGER.error("{}, Retrying in 5 seconds", aThrowable.toString());
		new Thread(() -> {
			try { Thread.sleep(5000); } catch (InterruptedException ignored) { }
			cClient.websocket(Server.PORT, Server.HOST, "/", Client::onConnected, Client::onFailureToConnect);
		}).start();
	}
	
	private static void onConnected(WebSocket socket) {
		LOGGER.info("Connection successful! Connected to {}", socket.remoteAddress());
		new Thread(){
			@Override
			public void run() {
				while (!isInterrupted()) try {
					Thread.sleep(1000);
					socket.writeBinaryMessage(cResolver.resolve(new PayloadWithInteger(123)));
					Thread.sleep(1000);
					socket.writeBinaryMessage(cResolver.resolve(new PayloadWithString("I did it!")));
					Thread.sleep(1000);
					socket.writeTextMessage("I am not resolvable! I am a generic text payload");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}.start();
	}
}
