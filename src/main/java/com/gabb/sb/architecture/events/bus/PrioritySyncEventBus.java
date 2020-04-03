package com.gabb.sb.architecture.events.bus;

import com.gabb.sb.architecture.events.IEvent;
import com.gabb.sb.architecture.events.IEventHandler;
import com.gabb.sb.architecture.events.bus.listener.SyncEventListener;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import static com.gabb.sb.Loggers.EVENT_BUS_LOGGER;

/**
 * Periodically drain queue to temp queue, then process everything in temp queue. This occurs at
 * a period of {@link PrioritySyncEventBus#getProcessingPeriodMillis()}. {@link PrioritySyncEventBus#beforeProcessing()}
 * and {@link PrioritySyncEventBus#afterProcessing()} are invoked before and after processing of said temp queue, respectively.
 * Events are processed according to their {@link IEvent#getPriority()}
 */
public abstract class PrioritySyncEventBus extends AbstractEventBus<SyncEventListener> {

	public static final int INIT_CAPACITY = 10;
	private PriorityBlockingQueue<IEvent> oSerialEventQueue;
	private Queue<IEvent> oEventDrain;

	public PrioritySyncEventBus(){
		oSerialEventQueue = new PriorityBlockingQueue<>(INIT_CAPACITY, Comparator.comparingInt(IEvent::getPriority));
		oEventDrain = new LinkedList<>();
	}

	public void start(){
		new Thread(){
			@Override
			public void run() {
				while(!isInterrupted()) try {
					sleep(getProcessingPeriodMillis());
					beforeProcessing();
					processQueue();
					afterProcessing();
				} catch (InterruptedException intEx){
					EVENT_BUS_LOGGER.error("Processing Loop Thread of PrioritySyncEventBus implementation '{}' Interrupted",
							this.getClass().getSimpleName(), intEx);
					interrupt();
				}
				catch (Throwable thrown) {
					if (handleExceptionReturnWhetherToStopProcessing(thrown)) break;
				}
				EVENT_BUS_LOGGER.error("Processing Loop Thread of PrioritySyncEventBus implementation '{}' has died", this.getClass().getSimpleName());
				if(restartUponDeath()) PrioritySyncEventBus.this.start();
			}
		}.start();
		EVENT_BUS_LOGGER.info("Event Bus '{}' started processing", this.getClass().getSimpleName());
	}

	@SuppressWarnings("unchecked")
	private void processQueue() {
		oSerialEventQueue.drainTo(oEventDrain);
		IEvent next;
		while(null != (next = oEventDrain.poll())){
			var listeners = oListenerMap.get(next.getClass());
			if (listeners == null) continue;
			for(var listener: listeners){
				listener.handleEvent(next);
			}
		}
	}


	@Override
	public void push(IEvent aEvent) {
		oSerialEventQueue.add(aEvent);
	}

	@Override
	public <E extends IEvent> void addListener(Class<E> aClass, IEventHandler<E> aHandler){
		addListener(new SyncEventListener<>(aClass) {
			@Override
			public void handleEvent(E aE) {
				aHandler.handle(aE);
			}
		});
	}

	protected abstract boolean restartUponDeath();
	protected abstract int getProcessingPeriodMillis();
	protected abstract boolean handleExceptionReturnWhetherToStopProcessing(Throwable aThrown);
	protected abstract void beforeProcessing();
	protected abstract void afterProcessing();
}
