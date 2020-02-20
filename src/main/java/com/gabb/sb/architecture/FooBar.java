package com.gabb.sb.architecture;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FooBar implements  IPayload{

	public FooBar() {
	}

	public FooBar(String aBar) {
		bar = aBar;
	}

	@JsonProperty
	String bar;
	
}
