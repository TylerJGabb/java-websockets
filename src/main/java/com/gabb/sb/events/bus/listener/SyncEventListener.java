package com.gabb.sb.events.bus.listener;

import com.gabb.sb.events.IEvent;

public abstract class SyncEventListener<E extends IEvent> extends AbstractEventListener<E> {
	
	public SyncEventListener(Class<E> aEventType) {
		super(aEventType);
	}
}
