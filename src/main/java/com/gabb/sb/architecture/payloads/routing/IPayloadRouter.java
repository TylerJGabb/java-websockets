package com.gabb.sb.architecture.payloads.routing;

import com.gabb.sb.architecture.listners.IListener;
import com.gabb.sb.architecture.payloads.IPayload;

/**
 * Takes a payload, and routes it to a listener. 
 */
public interface IPayloadRouter {

	/**
	 * @param aListener
	 * @return true if the listener was already added, or conflicts with another listening
	 * for the same IPayload
	 */
	boolean registerListener(IListener aListener);

	/**
	 * @param aPayload
	 * @return whether or not the payload was handled
	 */
	boolean route(IPayload aPayload);
	
}
