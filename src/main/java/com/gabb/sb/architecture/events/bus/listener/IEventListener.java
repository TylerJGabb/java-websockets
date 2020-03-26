package com.gabb.sb.architecture.events.bus.listener;

import com.gabb.sb.architecture.events.bus.IEvent;

public interface IEventListener<E extends IEvent> {
	
	void handleEvent(E aE);
	
	void queueEvent(E aE);
	
	Class<E> getEventType();
	
}
