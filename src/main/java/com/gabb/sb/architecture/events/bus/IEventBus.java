package com.gabb.sb.architecture.events.bus;

import com.gabb.sb.architecture.events.bus.listener.IEventListener;

public interface IEventBus {
	
	void push(IEvent aIEvent);
	
	void addListener(IEventListener aIEventListener);
	
	void removeListener(IEventListener aIEventListener);
	
}
