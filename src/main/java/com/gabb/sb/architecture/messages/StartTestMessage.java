package com.gabb.sb.architecture.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StartTestMessage extends AbstractMessage {
	
	@JsonProperty
	public String buildPath;
	
	@JsonProperty
	public String cucumberArgs;
	
	@JsonProperty
	public Integer runId;
	

	public StartTestMessage() { }

	public StartTestMessage(String aBuildPath, String aCucumberArgs, Integer aRunId) {
		buildPath = aBuildPath;
		cucumberArgs = aCucumberArgs;
		runId = aRunId;
	}
}
