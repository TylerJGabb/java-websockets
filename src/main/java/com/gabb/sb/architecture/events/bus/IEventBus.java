package com.gabb.sb.architecture.events.bus;

import com.gabb.sb.architecture.events.IEvent;
import com.gabb.sb.architecture.events.IEventHandler;
import com.gabb.sb.architecture.events.bus.listener.IEventListener;

public interface IEventBus<L extends IEventListener> {
	
	void push(IEvent aIEvent);

	void addListener(L aIEventListener);
	
	<E extends IEvent> void addListener(Class<E> aCLass, IEventHandler<E> handler);
	
	void removeListener(L aIEventListener);
	
}
