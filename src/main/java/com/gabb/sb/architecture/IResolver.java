package com.gabb.sb.architecture;

import io.vertx.core.buffer.Buffer;

/**
 * Resolves raw binary data into a Type and Payload
 */
public interface IResolver {

	/**
	 * Given a {@link Buffer} presumably from the incoming payload of a websocket connection, resolves it
	 * into one of a set of predefined types that are registered via {@link IResolver#registerTypeCode(Class, int)}
	 * 
	 * @return null if unable to resolve, else the sucessfully resolved IPayload. 
	 * 
	 * Still some brainstorming to do as to how to implement StrategyPattern (i.e. IResolverStrategy)
	 * 
	 */
	IPayload resolve(Buffer buf);

	/**
	 * registers a type with this resolver, making it capable of resolving
	 * Payloads of that type. If you try to resolve a Payload that is of a type
	 * not registered, it will not be resolved
	 * @param clazz
	 * @param code
	 * @return true if type was already registered
	 */
	boolean registerTypeCode(Class<? extends IPayload> clazz, int code);
	
}
