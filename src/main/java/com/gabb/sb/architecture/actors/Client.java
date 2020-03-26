package com.gabb.sb.architecture.actors;

import ch.qos.logback.classic.Level;
import com.gabb.sb.architecture.Util;
import com.gabb.sb.architecture.Server;
import com.gabb.sb.architecture.events.concretes.EventWithInteger;
import com.gabb.sb.architecture.events.concretes.EventWithString;
import com.gabb.sb.architecture.events.resolver.IEventResolver;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * This class is purely for sandboxing, but the way its designed would make for a really great
 * starting point for a new implementation of {@link com.gabb.sb.architecture.connection_integrity.IKeepAlive} 
 * but on client side. 
 */
public class Client {

	private static final Random RANDOM = new Random();
	private static Buffer[] cBuffers;

	private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
	private static HttpClient cClient;
	public static void main(String[] args) {
		Util.configureLoggersProgrammatically(Level.INFO);
		IEventResolver mCResolver = Util.testJsonResolver();
		cClient = Vertx.vertx().createHttpClient();
		cBuffers = new Buffer[]{
				mCResolver.resolve(new EventWithInteger(123)),
				mCResolver.resolve(new EventWithString("I did it!")),
				Buffer.buffer("I am not resolvable! I am a generic text message")
		};
		connect();
	}

	private static void connect() {
		LOGGER.info("Attempting to connect...");
		cClient.websocket(Server.PORT, Server.HOST, "/", Client::onConnected, Client::onFailureToConnect);
	}

	private static void onFailureToConnect(Throwable aThrowable){
		LOGGER.error("{}, Retrying in 5 seconds", aThrowable.toString());
		new Thread(() -> {
			try { Thread.sleep(5000); } catch (InterruptedException ignored) { }
			connect();
		}).start();
	}

	private static void onConnected(WebSocket socket) {
		LOGGER.info("Connection successful! Connected to {}", socket.remoteAddress());
		newRandoSender(socket).start();
	}

	private static Thread newRandoSender(WebSocket socket) {
		return new Thread(){
			@Override
			public void run() {
				while (!isInterrupted()) try {
					Buffer mRawDatum = randoBuf();
					LOGGER.info("Sending {}", mRawDatum);
					socket.write(mRawDatum);
					LOGGER.info("Sleeping 1000ms");
					Thread.sleep(1000);
				} catch (Exception ex) {
					LOGGER.error("Encountered exception when sending oRandom data, terminating and attempting to reconnect");
					interrupt();
					connect();
					break;
				}
				LOGGER.info("Randomized socket writer expired...");
			}
		};
	}

	private static Buffer randoBuf() {
		return cBuffers[RANDOM.nextInt(3)];
	}
}
