package com.gabb.sb.architecture.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageWithInteger extends AbstractMessage {

	//seems that if I name this field 'oInt' that the json encoder that comes with vertx
	//adds an additional property during serialization...
	@JsonProperty
	public int theInteger;

	public MessageWithInteger() { }

	public MessageWithInteger(int aInt) {
		theInteger = aInt;
	}

	public int getTheInteger() {
		return theInteger;
	}
}
