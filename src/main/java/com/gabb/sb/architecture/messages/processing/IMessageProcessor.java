package com.gabb.sb.architecture.messages.processing;

import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.messages.dispatching.IMessageDispatcher;

public interface IMessageProcessor<T extends IMessage> {

	void process(T payload);

	/**
	 * Returns the runtime type that this {@link IMessageProcessor} is capable of processing. 
	 * Used when dispatching a payload to a processor in an 
	 * {@link IMessageDispatcher#dispatch(IMessage)}
	 */
	Class canProcess();
	
}
