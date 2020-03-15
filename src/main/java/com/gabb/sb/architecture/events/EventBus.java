package com.gabb.sb.architecture.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class EventBus implements IEventBus {

	
	private final Logger oLogger;
	private final ConcurrentHashMap<Class<? extends IEvent>, List<IEventListener>> oListenerMap;

	public EventBus() {
		oLogger = LoggerFactory.getLogger(this.getClass());
		oListenerMap = new ConcurrentHashMap<>();
	}

	@Override
	public void push(IEvent e) {
		var listeners = oListenerMap.get(e.getClass());
		if (listeners == null) return;
		listeners.forEach(l -> l.queueEvent(e));
	}
	
	@Override
	public void addListener(IEventListener aIEventListener) {
		var eventType = aIEventListener.getEventType();
		oListenerMap.computeIfAbsent(eventType, __ -> new ArrayList<>());
		oListenerMap.get(eventType).add(aIEventListener);
	}

	@Override
	public void removeListener(IEventListener aIEventListener) {
		var listeners = oListenerMap.get(aIEventListener.getEventType());
		if (listeners == null) return;
		listeners.remove(aIEventListener);
	}

}
