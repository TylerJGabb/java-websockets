package com.gabb.sb;

import ch.qos.logback.classic.Level;
import com.gabb.sb.architecture.WhiteBoard;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;

public class Client {


	private static WebSocket socket;

	public static void main(String[] args) {
		App.configureLoggersProgrammatically(Level.INFO);
		HttpClient client = Vertx.vertx().createHttpClient();
		client.websocket(App.PORT, App.HOST, "/", res -> socket = res);
		while (true) try {
			Thread.sleep(1000);
			socket.writeBinaryMessage(WhiteBoard.getFooBar());
			Thread.sleep(1000);
			socket.writeBinaryMessage(WhiteBoard.getMessage());
			Thread.sleep(1000);
			socket.writeTextMessage("I am not resolvable!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
