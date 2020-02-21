package com.gabb.sb.architecture.payloads.dispatching;

import com.gabb.sb.architecture.payloads.processing.IPayloadProcessor;
import com.gabb.sb.architecture.payloads.IPayload;

/**
 * Takes a payload, and routes it to a listener. 
 */
public interface IPayloadDispatcher {

	/**
	 * @param aListener
	 * @return true if the listener was already added, or conflicts with another listening
	 * for the same IPayload
	 */
	boolean registerConsumer(IPayloadProcessor aListener);

	/**
	 * @param aPayload
	 * @return whether or not the payload was handled
	 */
	boolean route(IPayload aPayload);
	
}
