package com.gabb.sb.architecture;

public interface IPayloadBroker {

	/**
	 * @param aListener
	 * @return true if the listener did not already exist in this's internal collection
	 */
	boolean registerListener(IListener aListener);

	/**
	 * @param aPayload
	 * @return whether or not the payload was handled
	 */
	boolean delegate(IPayload aPayload);
	
}
