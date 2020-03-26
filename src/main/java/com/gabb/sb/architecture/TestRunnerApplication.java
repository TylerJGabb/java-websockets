package com.gabb.sb.architecture;

import ch.qos.logback.classic.Level;

public class TestRunnerApplication {

	/**
	 * TODO: GET RID OF THIS!!!
	 */
	public static void main(String[] args) {
		Util.configureLoggersProgrammatically(Level.INFO);
		var client = new KeepAliveClient(Server.PORT, Server.HOST);
		client.connect();
	}
	
}
