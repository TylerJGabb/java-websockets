package com.gabb.sb.architecture.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageWithString extends AbstractMessage {

	@JsonProperty("string")
	String oString;

	public MessageWithString() { }

	public MessageWithString(String aStr) {
		oString = aStr;
	}

	public String getString() {
		return oString;
	}
}
