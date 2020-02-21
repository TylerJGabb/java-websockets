package com.gabb.sb.architecture.payloads.processing;

import com.gabb.sb.architecture.payloads.IPayload;

public interface IPayloadProcessor<T extends IPayload> {

	void consume(T payload);

	/**
	 * Returns the runtime type that this {@link IPayloadProcessor} is capable of consuming. 
	 * Used when dispatching a payload to a consumer in an 
	 * {@link com.gabb.sb.architecture.payloads.dispatching.IPayloadDispatcher}
	 */
	Class canConsume();
	
}
