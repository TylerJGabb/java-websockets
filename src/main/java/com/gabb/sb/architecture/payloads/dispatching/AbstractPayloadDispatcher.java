package com.gabb.sb.architecture.payloads.dispatching;


import com.gabb.sb.architecture.payloads.processing.IPayloadProcessor;
import com.gabb.sb.architecture.payloads.IPayload;

import java.util.HashMap;

/**
 * When an incoming {@link io.vertx.core.buffer.Buffer} is resolved by an
 * {@link com.gabb.sb.architecture.resolver.IResolver} into an {@link IPayload},
 * it is then passed to an instance of this class, who then finds an
 * {@link IPayloadProcessor} capable of processing said payload. 
 */
public abstract class AbstractPayloadDispatcher implements IPayloadDispatcher {
	//TODO: make thread safe??

	private HashMap<Class<? extends IPayload>, IPayloadProcessor> oListeners;

	protected AbstractPayloadDispatcher() {
		oListeners = new HashMap<>();
	}

	@Override
	public boolean registerConsumer(IPayloadProcessor aListener) {
		Class<? extends IPayload> mClass = aListener.canConsume();
		if(oListeners.containsKey(mClass)){
			System.out.println("AbstractPayloadProcessor already registered for " + mClass);
			return true;
		}
		oListeners.put(mClass, aListener);
		return false;
	}

	@Override
	public boolean route(IPayload aPayload) {
		IPayloadProcessor mIListener = oListeners.get(aPayload.getClass());
		if(mIListener != null){
			mIListener.consume(aPayload);
			return true;
		}
		return false;
	}
}


