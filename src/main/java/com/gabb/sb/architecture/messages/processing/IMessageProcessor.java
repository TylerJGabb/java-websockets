package com.gabb.sb.architecture.messages.processing;

import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.messages.dispatching.IMessageDispatcher;

public interface IMessageProcessor<T extends IMessage> {

	void process(T message);

	/**
	 * Returns the runtime type that this {@link IMessageProcessor} is capable of processing. 
	 * Used when dispatching an {@link IMessage} to a processor in
	 * {@link IMessageDispatcher#dispatch(IMessage)}
	 */
	Class canProcess();
	
}
