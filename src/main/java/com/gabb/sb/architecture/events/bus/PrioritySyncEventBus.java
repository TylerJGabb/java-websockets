package com.gabb.sb.architecture.events.bus;

import com.gabb.sb.architecture.events.IEvent;
import com.gabb.sb.architecture.events.IEventHandler;
import com.gabb.sb.architecture.events.bus.listener.SyncEventListener;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

//periodically drain queue to temp queue, process everything in queue, then perform special logic
public abstract class PrioritySyncEventBus extends AbstractEventBus<SyncEventListener> {

	public static final int INIT_CAPACITY = 10;
	private PriorityBlockingQueue<IEvent> oSerialEventQueue;
	private Queue<IEvent> oEventDrain;
	private Thread oProcessLoopThread;
	
	public PrioritySyncEventBus(){
		oSerialEventQueue = new PriorityBlockingQueue<>(INIT_CAPACITY, Comparator.comparingInt(IEvent::getPriority));
		oEventDrain = new LinkedList<>();
		oProcessLoopThread = makeProcessLoopThread();
	}
	
	public void start(){
		oLogger.info("DCEB STARTING");
		oProcessLoopThread.start();
	}
	
	@SuppressWarnings("unchecked")
	private Thread makeProcessLoopThread() {
		return new Thread(() -> {
			while(!Thread.currentThread().isInterrupted()) try {
				Thread.sleep(500);
				beforeProcessing();
				oSerialEventQueue.drainTo(oEventDrain);
				IEvent next;
				while(null != (next = oEventDrain.poll())){
					var listeners = oListenerMap.get(next.getClass());
					if (listeners == null) continue;
					for(var listener: listeners){
						listener.handleEvent(next);
					}
				}
				afterProcessing();
			} catch (InterruptedException intEx){
				oLogger.error("Processing Loop Interrupted", intEx);
				Thread.currentThread().interrupt();
			}
			catch (Throwable thrown) {
				if (handleException(thrown)) return;
			}
			System.out.println("DCEB HAS DIED");
		});
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

	protected abstract boolean handleException(Throwable aThrown);
	protected abstract void beforeProcessing();
	protected abstract void afterProcessing();
}
