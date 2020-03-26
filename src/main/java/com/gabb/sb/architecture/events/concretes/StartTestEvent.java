package com.gabb.sb.architecture.events.concretes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.architecture.events.bus.IEvent;

public class StartTestEvent implements IEvent {
	
	@JsonProperty
	public String buildPath;
	
	@JsonProperty
	public String cucumberArgs;
	
	@JsonProperty
	public Integer runId;
	

	public StartTestEvent() { }

	public StartTestEvent(String aBuildPath, String aCucumberArgs, Integer aRunId) {
		buildPath = aBuildPath;
		cucumberArgs = aCucumberArgs;
		runId = aRunId;
	}
}
