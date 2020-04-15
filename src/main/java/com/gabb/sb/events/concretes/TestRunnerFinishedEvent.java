package com.gabb.sb.events.concretes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.Status;
import com.gabb.sb.events.IEvent;

public class TestRunnerFinishedEvent implements IEvent {

	@JsonProperty
	public Status result;
	
	@JsonProperty
	public String logFilesLocation;
	
	@JsonProperty 
	public Integer runId;

	public TestRunnerFinishedEvent(Status aResult, String aLogFilesLocation, Integer aRunId) {
		result = aResult;
		logFilesLocation = aLogFilesLocation;
		runId = aRunId;
	}

	public TestRunnerFinishedEvent() { }

	@Override
	public int getPriority() {
		return 1;
	}
}
