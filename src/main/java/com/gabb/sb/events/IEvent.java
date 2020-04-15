package com.gabb.sb.events;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IEvent { 
	
	@JsonIgnore
	default int getPriority(){
		return Integer.MAX_VALUE;
	}
	
}
