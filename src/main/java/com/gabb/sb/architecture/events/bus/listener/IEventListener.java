package com.gabb.sb.architecture.events.bus.listener;

import com.gabb.sb.architecture.events.IEvent;

public interface IEventListener<E extends IEvent> {
	
	void handleEvent(E aE);
	
	Class<E> getEventType();
	
}
