package com.gabb.sb.events.concretes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.events.IEvent;

public class StartRunEvent implements IEvent {
	
	@JsonProperty
	public String buildPath;
	
	@JsonProperty
	public String cucumberArgs;
	
	@JsonProperty
	public Integer runId;
	

	public StartRunEvent() { }

	public StartRunEvent(String aBuildPath, String aCucumberArgs, Integer aRunId) {
		buildPath = aBuildPath;
		cucumberArgs = aCucumberArgs;
		runId = aRunId;
	}

	@Override
	public int getPriority() {
		return 2;
	}
}
