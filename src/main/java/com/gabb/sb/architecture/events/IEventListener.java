package com.gabb.sb.architecture.events;

public interface IEventListener<E extends IEvent> {
	
	void handleEvent(E aE);
	
	void queueEvent(E aE);
	
	Class<E> getEventType();
	
}
