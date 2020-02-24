package com.gabb.sb.architecture.messages.dispatching;


import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.messages.processing.IMessageProcessor;
import com.gabb.sb.architecture.resolver.IMessageResolver;

import java.util.HashMap;

/**
 * When an incoming {@link io.vertx.core.buffer.Buffer} is resolved by an
 * {@link IMessageResolver} into an {@link IMessage},
 * it is then passed to an instance of this class, who then finds an
 * {@link IMessageProcessor} capable of processing said payload. 
 */
public abstract class AbstractMessageDispatcher implements IMessageDispatcher {
	//TODO: make thread safe??

	private HashMap<Class<? extends IMessage>, IMessageProcessor> oListeners;

	protected AbstractMessageDispatcher() {
		oListeners = new HashMap<>();
	}

	@Override
	public boolean registerPayloadProcessor(IMessageProcessor aListener) {
		Class<? extends IMessage> mClass = aListener.canProcess();
		if(oListeners.containsKey(mClass)){
			System.out.println("AbstractMessageProcessor already registered for " + mClass);
			return true;
		}
		oListeners.put(mClass, aListener);
		return false;
	}

	@Override
	public boolean dispatch(IMessage aPayload) {
		IMessageProcessor mIListener = oListeners.get(aPayload.getClass());
		if(mIListener != null){
			mIListener.process(aPayload);
			return true;
		}
		return false;
	}
}


