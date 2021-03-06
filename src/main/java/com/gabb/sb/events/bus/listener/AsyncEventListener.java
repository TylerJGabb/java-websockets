package com.gabb.sb.events.bus.listener;

import com.gabb.sb.events.IEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AsyncEventListener<E extends IEvent> extends AbstractEventListener<E> {

	private final BlockingQueue<E> oQueue;

	public AsyncEventListener(Class<E> aEventType) {
		super(aEventType);
		oQueue = new LinkedBlockingQueue<>();
		new Thread(){
			@Override
			public void run() {
				while(!isInterrupted()) try {
					handleEvent(oQueue.take());
				} catch (InterruptedException aE) {
					aE.printStackTrace();
					interrupt();
				}
			}
		}.start();
	}

	public final void queueEvent(E aE) {
		oQueue.add(aE);
	}
}
