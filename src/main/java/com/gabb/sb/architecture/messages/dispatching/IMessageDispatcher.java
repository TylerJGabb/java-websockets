package com.gabb.sb.architecture.messages.dispatching;

import com.gabb.sb.architecture.messages.processing.IMessageProcessor;
import com.gabb.sb.architecture.messages.IMessage;

/**
 * A class that facilitates processing of messages coming through web sockets
 */
public interface IMessageDispatcher {

	/**
	 * Registers a {@link IMessageProcessor} to this dispatcher.
	 * @param aListener
	 * @return
	 */
	boolean registerMessageProcessor(IMessageProcessor aListener);

	/**
	 * Dispatches an {@link IMessage} to a {@link IMessageProcessor} that was registered to this dispatcher via
	 * {@link IMessageDispatcher#registerMessageProcessor(IMessageProcessor)}.
	 * @return false if no processor has been registered with this dispatcher
	 * such that {@link IMessageProcessor#canProcess()}.equals(message.getClass()),
	 * otherwise true
	 */
	boolean dispatch(IMessage message);
	
}
