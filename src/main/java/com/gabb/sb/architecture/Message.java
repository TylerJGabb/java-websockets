package com.gabb.sb.architecture;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message implements IPayload{

	public Message() {
	}

	public Message(int aFoo) {
		foo = aFoo;
	}

	@JsonProperty
	int foo;
}
