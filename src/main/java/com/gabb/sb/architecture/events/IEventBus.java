package com.gabb.sb.architecture.events;

public interface IEventBus {
	
	void push(IEvent aIEvent);
	
	void addListener(IEventListener aIEventListener);
	
	void removeListener(IEventListener aIEventListener);
	
}
