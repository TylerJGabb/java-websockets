package com.gabb.sb.architecture.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayloadWithInteger implements IPayload {

	//seems that if I name this field 'oInt' that the json encoder that comes with vertx
	//adds an additional property during serialization...
	@JsonProperty
	public int theInteger;

	public PayloadWithInteger() { }

	public PayloadWithInteger(int aInt) {
		theInteger = aInt;
	}

	public int getTheInteger() {
		return theInteger;
	}
}
