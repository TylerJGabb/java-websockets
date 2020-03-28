package com.gabb.sb.architecture.events.bus.listener;

import com.gabb.sb.architecture.events.IEvent;

public abstract class SyncEventListener<E extends IEvent> extends AbstractEventListener<E> {
	
	public SyncEventListener(Class<E> aEventType) {
		super(aEventType);
	}
}
