package com.gabb.sb.architecture.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestRunnerFinished extends AbstractMessage {
	
	@JsonProperty
	public String result;
	
	@JsonProperty
	public String logFilesLocation;

	public TestRunnerFinished(String aResult, String aLogFilesLocation) {
		result = aResult;
		logFilesLocation = aLogFilesLocation;
	}

	public TestRunnerFinished() { }
}
