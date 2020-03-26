package com.gabb.sb.architecture.events.concretes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.architecture.events.bus.IEvent;

public class TestRunnerFinishedEvent implements IEvent {

	@JsonProperty
	public String result;
	
	@JsonProperty
	public String logFilesLocation;
	
	@JsonProperty 
	public Integer runId;

	public TestRunnerFinishedEvent(String aResult, String aLogFilesLocation, Integer aRunId) {
		result = aResult;
		logFilesLocation = aLogFilesLocation;
		runId = aRunId;
	}

	public TestRunnerFinishedEvent() { }
}
