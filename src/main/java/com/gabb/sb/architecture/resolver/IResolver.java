package com.gabb.sb.architecture.resolver;

import com.gabb.sb.architecture.payloads.IPayload;
import com.gabb.sb.architecture.resolver.strategies.IPayloadResolveStrategy;
import io.vertx.core.buffer.Buffer;

/**
 * Resolves raw binary data into a Type and Payload
 */
public interface IResolver {

	/**
	 * sets the strategy that this testDispatcher used to resolve incoming packets
	 * @param strategy
	 */
	
	void setStrategy(IPayloadResolveStrategy strategy);
	
	/**
	 * Given a {@link Buffer} presumably from the incoming payload of a websocket connection, resolves it
	 * into one of a set of predefined types that are registered via {@link IResolver#registerTypeCode(Class, int)}
	 * 
	 * @return null if unable to resolve, else the sucessfully resolved IPayload. 
	 *
	 */
	IPayload resolve(Buffer buf);


	/**
	 * Given an {@link IPayload} turns it into a buffer using the {@link IPayloadResolveStrategy} provided in call
	 * {@link IResolver#setStrategy(IPayloadResolveStrategy)}
	 * @param payload
	 * @return
	 */
	Buffer resolve(IPayload payload);

	/**
	 * registers a type with this resolver, making it capable of resolving
	 * Payloads of that type. If you try to resolve a Payload that is of a type
	 * not registered, it will not be resolved
	 * @param clazz
	 * @param code
	 * @return false if the type code was already registered, else true
	 */
	boolean registerTypeCode(Class<? extends IPayload> clazz, int code);

}
