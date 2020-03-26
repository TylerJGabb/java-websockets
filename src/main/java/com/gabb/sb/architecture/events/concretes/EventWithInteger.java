package com.gabb.sb.architecture.events.concretes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.architecture.events.bus.IEvent;

public class EventWithInteger implements IEvent {

	//seems that if I name this field 'oInt' that the json encoder that comes with vertx
	//adds an additional property during serialization...
	@JsonProperty
	private int theInteger;

	public EventWithInteger() { }

	public EventWithInteger(int aInt) {
		theInteger = aInt;
	}

	public int getTheInteger() {
		return theInteger;
	}
}
