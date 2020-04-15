package com.gabb.sb.events.bus;

import com.gabb.sb.events.IEvent;
import com.gabb.sb.events.IEventHandler;
import com.gabb.sb.events.bus.listener.AsyncEventListener;

@SuppressWarnings("unchecked")
public class ConcurrentEventBus extends AbstractEventBus<AsyncEventListener> {

	@Override
	public void push(IEvent e) {
		var listeners = oListenerMap.get(e.getClass());
		if (listeners == null) return;
		listeners.forEach(l -> l.queueEvent(e));
	}

	@Override
	public <E extends IEvent> void addListener(Class<E> aClass, IEventHandler<E> aHandler){
		addListener(new AsyncEventListener<>(aClass) {
			@Override
			public void handleEvent(E aE) {
				aHandler.handle(aE);
			}
		});
	}
	
	public static Builder builder(){
		return new Builder();
	}
	
	public static class Builder{
		
		private IEventBus oInstance;

		public Builder() {
			oInstance = new ConcurrentEventBus();
		}
		
		public <E extends IEvent> Builder addListener(Class<E> aClass, IEventHandler<E> aHandler){
			oInstance.addListener(new AsyncEventListener<>(aClass) {
				@Override
				public void handleEvent(E aEvent) {
					aHandler.handle(aEvent);
				}
			});
			return this;
		}
		
		public IEventBus build(){
			return oInstance;
		}
			
	}
}
