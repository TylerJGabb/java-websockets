package com.gabb.sb.architecture.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestRunnerFinishedMessage extends AbstractMessage {

	@JsonProperty
	public String result;
	
	@JsonProperty
	public String logFilesLocation;
	
	@JsonProperty 
	public Integer runId;

	public TestRunnerFinishedMessage(String aResult, String aLogFilesLocation, Integer aRunId) {
		result = aResult;
		logFilesLocation = aLogFilesLocation;
		runId = aRunId;
	}

	public TestRunnerFinishedMessage() { }
}
