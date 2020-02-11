package com.gabb.sb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.javafx.collections.MappingChange;

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
