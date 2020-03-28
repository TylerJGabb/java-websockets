package com.gabb.sb.architecture.events.bus.listener;

import com.gabb.sb.architecture.events.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventListener<E extends IEvent> implements IEventListener<E>{
	
	private final Class<E> oEventType;
	protected final Logger oLogger;

	public AbstractEventListener(Class<E> aEventType) {
		oEventType = aEventType;
		oLogger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public Class<E> getEventType() {
		return oEventType;
	}
}
