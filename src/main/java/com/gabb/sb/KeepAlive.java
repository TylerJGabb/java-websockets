package com.gabb.sb;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.Function;

class KeepAlive extends Thread {

	public static final int DEFAULT_PERIOD = 5000;
	public static final int OVERDUE_PONG_PERCENTAGE_LIMIT = 50;

	private long lastPongRecieved = 0;
	private int period;
	private final ServerWebSocket socket;
	private Function<ServerWebSocket, Boolean> overduePongHandler;
	private final Logger logger;

	public KeepAlive(ServerWebSocket socket) {
		super("KeepAlive for " + socket.remoteAddress());
		logger = LoggerFactory.getLogger(this.getClass());
		this.period = DEFAULT_PERIOD;
		socket.pongHandler(pong -> {
			logger.info("received pong from {}", socket.remoteAddress());
			this.lastPongRecieved = Instant.now().toEpochMilli();
		});
		this.socket = socket;
	}

	public KeepAlive(ServerWebSocket socket, int period) {
		this(socket);
		this.period = period;
	}
	
	//TODO: extract to functional interface

	/**
	 * A function that takes a ServerWebSocket and returns whether or not to 
	 * kill this KeepAlive
	 * @param handler
	 * @return
	 */
	public KeepAlive handleOverduePong(Function<ServerWebSocket, Boolean> handler) {
		this.overduePongHandler = handler;
		return this;
	}

	/**
	 * Handles the Overdue Pong and allows the KeepAlive to continue processing
	 * @param handler
	 * @return
	 */
	public KeepAlive handleOverduePong(Handler<ServerWebSocket> handler){
		this.overduePongHandler = s -> {
			handler.handle(s);
			return false;
		};
		return this;
	}
	
	@Override
	public void run() {
		logger.info("KeepAlive started");
		while (!isInterrupted()) try {
			Thread.sleep(period);
			if (isPongOverdue() && onOverduePong()) break;
			logger.info("sending ping");
			socket.writePing(Buffer.buffer());
		} catch (InterruptedException e) {
			interrupt();
			logger.info("KeepAlive interrupted");
			break;
		} catch (Exception e){
			interrupt();
			logger.error("encountered unexpected Exception", e);
			break;
		}
		logger.info("KeepAlive expired");
	}

	private boolean onOverduePong() {
		if (overduePongHandler != null) {
			logger.warn("invoking overduePongHandler...");
			//TODO: visit possibility of passing in "instructions" to be set by the caller
			//then interpreted on this end to tell whether or not to continue
			if(this.overduePongHandler.apply(socket)) {
				logger.warn("overduePongHandler applied and returned true, meaning I should stop. killing myself");
				return true;
			}
		}
		return false;
	}

	private boolean isPongOverdue() {
		if (lastPongRecieved > 0) {
			long millisPassedSinceLastPong = Instant.now().toEpochMilli() - lastPongRecieved;
			long overduePercentage = 100 * Math.abs(period - millisPassedSinceLastPong) / period;
			if (overduePercentage > OVERDUE_PONG_PERCENTAGE_LIMIT) {
				logger.warn("pong is overdue by {}%", overduePercentage);
				return true;
			}
		}
		return false;
	}

	public ServerWebSocket getSocket() {
		return socket;
	}
}
