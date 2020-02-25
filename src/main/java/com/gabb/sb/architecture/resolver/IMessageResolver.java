package com.gabb.sb.architecture.resolver;

import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.resolver.strategies.IMessageResolveStrategy;
import io.vertx.core.buffer.Buffer;

/**
 * Resolves raw binary data contained in a {@link Buffer} into an {@link IMessage}
 */
public interface IMessageResolver {

	/**
	 * Sets the {@link IMessageResolveStrategy} to be used by this {@link IMessageResolver}
	 */
	
	void setStrategy(IMessageResolveStrategy strategy);
	
	/**
	 * Given a {@link Buffer} presumably from the incoming payload of a web socket connection, resolves it
	 * into one of a set of predefined types that are registered via
	 * {@link IMessageResolver#registerTypeCode(Class, int)}
	 */
	IMessage resolve(Buffer buf) throws IllegalStateException;


	/**
	 * Given an {@link IMessage} turns it into a {@link Buffer} using the {@link IMessageResolveStrategy} 
	 * provided in a call to 
	 * {@link IMessageResolver#setStrategy(IMessageResolveStrategy)}
	 */
	Buffer resolve(IMessage message);

	/** 
	 * An {@link IMessage} can be implemented by many concretes, the job of this method is to register
	 * each one with a unique code to this resolver making it capable of resolving
	 * {@link IMessage}s of that type. If you try to resolve an {@link IMessage} that is of a type
	 * not registered, it will not be resolved by this resolver
	 * (this may make it hard to do this project using microservices since these type codes need to be shared across
	 * all services to make it possible to communicate with these messages, revisit library option)
	 * @param clazz the concrete class that implements {@link IMessage}
	 * @param code a globally unique integer that identifies this concrete class
	 * @return false if the type code was already registered
	 */
	boolean registerTypeCode(Class<? extends IMessage> clazz, int code);

}
