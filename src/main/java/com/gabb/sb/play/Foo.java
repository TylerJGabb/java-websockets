package com.gabb.sb.play;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class Foo {
	
	@JsonProperty
	String foo;
	
	@JsonProperty
	Map<String, String> map;

	public Foo() {
		map = new HashMap<>();
	}

	public Foo(String foo) {
		this();
		this.foo = foo;
	}
}
