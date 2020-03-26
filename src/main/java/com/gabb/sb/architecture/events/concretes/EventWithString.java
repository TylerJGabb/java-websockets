package com.gabb.sb.architecture.events.concretes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.architecture.events.bus.IEvent;

public class EventWithString implements IEvent {

	@JsonProperty("string")
	String oString;

	public EventWithString() { }

	public EventWithString(String aStr) {
		oString = aStr;
	}

	public String getString() {
		return oString;
	}
}
