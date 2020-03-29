package com.gabb.sb.architecture;

import ch.qos.logback.classic.Level;

import java.io.IOException;

public class TestRunnerApplication {

	/**
	 * TODO: GET RID OF THIS!!!
	 */
	public static void main(String[] args) throws IOException {
		var props = System.getProperties();
		var tags = TestRunnerApplication.class.getClassLoader().getResourceAsStream("testrunner.properties");
		if(tags != null) props.load(tags);
		Util.configureLoggersProgrammatically(Level.INFO);
		var client = new KeepAliveClient(Server.PORT, Server.HOST);
		client.connect();
	}
	
}
