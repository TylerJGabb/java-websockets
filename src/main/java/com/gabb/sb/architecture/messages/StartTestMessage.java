package com.gabb.sb.architecture.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StartTestMessage extends AbstractMessage {
	
	@JsonProperty
	public String buildPath;
	
	@JsonProperty
	public String cucumberArgs;

	public StartTestMessage() { }

	public StartTestMessage(String aBuildPath, String aCucumberArgs) {
		buildPath = aBuildPath;
		cucumberArgs = aCucumberArgs;
	}
}
