package com.gabb.sb.architecture.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractMessage implements IMessage {

	@JsonProperty
	String oSource;
	
	@JsonProperty
	String oDestination;
	
	public void setSource(String aSource) {
		oSource = aSource;
	}

	public void setDestination(String aDestination) {
		oDestination = aDestination;
	}

	@Override
	public String getSource() {
		return oSource;
	}

	@Override
	public String getDestination() {
		return oDestination;
	}
}
