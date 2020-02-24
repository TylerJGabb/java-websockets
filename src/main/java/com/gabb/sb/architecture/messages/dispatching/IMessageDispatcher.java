package com.gabb.sb.architecture.messages.dispatching;

import com.gabb.sb.architecture.messages.processing.IMessageProcessor;
import com.gabb.sb.architecture.messages.IMessage;

/**
 * Takes a payload, and routes it to a listener. 
 */
public interface IMessageDispatcher {

	/**
	 * @param aListener
	 * @return true if the listener was already added, or conflicts with another listening
	 * for the same IMessage
	 */
	boolean registerPayloadProcessor(IMessageProcessor aListener);

	/**
	 * Dispatches the payload to a {@link IMessageProcessor} that was registered to this dispatcher via
	 * {@link IMessageDispatcher#registerPayloadProcessor(IMessageProcessor)}.
	 * @param aPayload
	 * @return false if no processor has been registered with this dispatcher
	 * such that {@link IMessageProcessor#canProcess()}.equals(aPayload.getClass()),
	 * otherwise true
	 */
	boolean dispatch(IMessage aPayload);
	
}
