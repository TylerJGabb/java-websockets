package com.gabb.sb.architecture;

import ch.qos.logback.classic.Level;

import java.io.IOException;

public class TestRunnerApplication {

	//used for testing purposes
	public static boolean NO_FINISH = false;

	/**
	 * TODO: GET RID OF THIS!!!
	 */
	public static void main(String[] args) throws IOException {
		var sysProps = System.getProperties();
		var runnerProps = TestRunnerApplication.class.getClassLoader().getResourceAsStream("testrunner.properties");
		if(runnerProps != null) sysProps.load(runnerProps);
		Util.configureLoggersProgrammatically(Level.INFO);
		var client = new KeepAliveClient(Server.PORT, Server.HOST);
		client.connect();
		String finish = System.getProperty("finish");
		NO_FINISH = finish == null || !Boolean.parseBoolean(finish);
		System.out.println(NO_FINISH ? "PROPERTY FILE SPECIFIED NO RESPONSE, WILL NOT RESPOND" : "PROPERTY FILE SPECIFIED RESPONSE, WILL RESPOND");
	}
	
}
