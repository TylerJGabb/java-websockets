package com.gabb.sb;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

import java.time.Instant;

import static com.gabb.sb.App.LOGGER;

class KeepAlive extends Thread {

	public static final int DEFAULT_PERIOD = 5000;
	private final String id;

	private long lastPongRecieved = 0;
	private int period;
	private ServerWebSocket socket;
	private Handler<ServerWebSocket> missedPongHandler;


	public KeepAlive(ServerWebSocket socket){
		this.id = Integer.toHexString(socket.hashCode()) + "-" + socket.remoteAddress();
		this.period = DEFAULT_PERIOD;
		this.socket = socket;
		this.socket.pongHandler(pong -> {
			LOGGER.info("{} received pong", this.id);
			this.lastPongRecieved = Instant.now().toEpochMilli();
		});
	}

	public KeepAlive(ServerWebSocket socket, int period) {
		this(socket);
		this.period = period;
	}
	
	public KeepAlive handleMissedPong(Handler<ServerWebSocket> handler){
		this.missedPongHandler = handler;
		return this;
	}

	@Override
	public void run() {
		LOGGER.info("KeepAlive started for {}", id);
		while (!isInterrupted()) try {
			Thread.sleep(period);
			if(lastPongRecieved > 0){
				long millisPassedSinceLastPong = Instant.now().toEpochMilli() - lastPongRecieved;
				long expectedPongPeriodDiffPercentage = 100 * Math.abs(period - millisPassedSinceLastPong) / period;
				if(expectedPongPeriodDiffPercentage > 50){
					LOGGER.warn("Missed Pong for {}", id);
					if(missedPongHandler != null) missedPongHandler.handle(socket);
				}
			}
			LOGGER.info("KeepAlive Pinging {}", id);
			socket.writePing(Buffer.buffer());
		} catch (InterruptedException e) {
			interrupt();
			LOGGER.info("KeepAlive interrupted for {}", id);
			break;
		}
		LOGGER.info("KeepAlive expired for {}", id);
	}

	private String id() {
		return this.id;
	}

	public ServerWebSocket getSocket() {
		return socket;
	}
}
