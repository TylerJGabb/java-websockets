package com.gabb.sb.architecture.events.bus;

import com.gabb.sb.architecture.events.IEvent;
import com.gabb.sb.architecture.events.bus.listener.AbstractEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractEventBus<L extends AbstractEventListener> implements IEventBus<L> {

	protected final Logger oLogger;
	protected final ConcurrentHashMap<Class<? extends IEvent>, List<L>> oListenerMap;
	
	public AbstractEventBus() {
		oLogger = LoggerFactory.getLogger(this.getClass());
		oListenerMap = new ConcurrentHashMap<>();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addListener(L aIEventListener) {
		var eventType = aIEventListener.getEventType();
		oListenerMap.computeIfAbsent(eventType, __ -> new ArrayList<>());
		oListenerMap.get(eventType).add(aIEventListener);
	}

	@Override
	public void removeListener(L aIEventListener) {
		var listeners = oListenerMap.get(aIEventListener.getEventType());
		if (listeners == null) return;
		listeners.remove(aIEventListener);
	}
}
