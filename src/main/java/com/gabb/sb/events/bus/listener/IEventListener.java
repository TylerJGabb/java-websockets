package com.gabb.sb.events.bus.listener;

import com.gabb.sb.events.IEvent;

public interface IEventListener<E extends IEvent> {
	
	void handleEvent(E aE);
	
	Class<E> getEventType();
	
}
