package com.gabb.sb.architecture.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayloadWithString implements IPayload {

	@JsonProperty("string")
	String oString;

	public PayloadWithString() { }

	public PayloadWithString(String aStr) {
		oString = aStr;
	}

	public String getString() {
		return oString;
	}
}
