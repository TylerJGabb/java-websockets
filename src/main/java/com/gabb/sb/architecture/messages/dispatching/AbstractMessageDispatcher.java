package com.gabb.sb.architecture.messages.dispatching;


import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.messages.processing.IMessageProcessor;
import com.gabb.sb.architecture.resolver.IMessageResolver;

import java.util.HashMap;

/**
 * When an incoming {@link io.vertx.core.buffer.Buffer} is resolved by an
 * {@link IMessageResolver} into an {@link IMessage},
 * it is then passed to an instance of this class, who then finds an
 * {@link IMessageProcessor} capable of processing it.
 */
public abstract class AbstractMessageDispatcher implements IMessageDispatcher {
	//TODO: make thread safe??

	private HashMap<Class<? extends IMessage>, IMessageProcessor> oMessageProcessorMap;

	protected AbstractMessageDispatcher() {
		oMessageProcessorMap = new HashMap<>();
	}

	@Override
	public boolean registerMessageProcessor(IMessageProcessor messageProcessor) {
		Class<? extends IMessage> mClass = messageProcessor.canProcess();
		if(oMessageProcessorMap.containsKey(mClass)){
			System.out.println("AbstractMessageProcessor already registered for " + mClass);
			return true;
		}
		oMessageProcessorMap.put(mClass, messageProcessor);
		return false;
	}

	@Override
	public boolean dispatch(IMessage message) {
		IMessageProcessor processor = oMessageProcessorMap.get(message.getClass());
		if(processor != null){
			processor.process(message);
			return true;
		}
		return false;
	}
}


