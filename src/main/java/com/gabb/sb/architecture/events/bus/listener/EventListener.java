package com.gabb.sb.architecture.events.bus.listener;

import com.gabb.sb.architecture.events.bus.IEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class EventListener<E extends IEvent> implements IEventListener<E> {

	private final Class<E> oEventType;
	private final BlockingQueue<E> oQueue;

	public EventListener(Class<E> aEventType) {
		oEventType = aEventType;
		oQueue = new LinkedBlockingQueue<>();
		new Thread(){
			@Override
			public void run() {
				while(!isInterrupted()) try {
					handleEvent(oQueue.take());
				} catch (InterruptedException aE) {
					aE.printStackTrace();
				}
			}
		}.start();
	}

	@Override
	public final Class<E> getEventType() {
		return oEventType;
	}

	@Override
	public final void queueEvent(E aE) {
		oQueue.add(aE);
	}
}
