package com.gabb.sb.architecture.events.resolver;

import com.gabb.sb.architecture.events.bus.IEvent;
import com.gabb.sb.architecture.events.resolver.strategies.IEventResolveStrategy;
import io.vertx.core.buffer.Buffer;

public interface IEventResolver {

	/**
	 * Sets the {@link IEventResolveStrategy} to be used by this {@link IEventResolver}
	 */
	
	void setStrategy(IEventResolveStrategy strategy);
	
	/**
	 * Given a {@link Buffer} presumably from the incoming payload of a web socket connection, resolves it
	 * into one of a set of predefined types that are registered via
	 * {@link IEventResolver#registerTypeCode(Class, int)}
	 */
	IEvent resolve(Buffer buf) throws IllegalStateException;


	/**
	 * Given an {@link IEvent} turns it into a {@link Buffer} using the {@link IEventResolveStrategy} 
	 * provided in a call to 
	 * {@link IEventResolver#setStrategy(IEventResolveStrategy)}
	 */
	Buffer resolve(IEvent message);

	/** 
	 * An {@link IEvent} can be implemented by many concretes, the job of this method is to register
	 * each one with a unique code to this resolver making it capable of resolving
	 * {@link IEvent}s of that type. If you try to resolve an {@link IEvent} that is of a type
	 * not registered, it will not be resolved by this resolver
	 * (this may make it hard to do this project using microservices since these type codes need to be shared across
	 * all services to make it possible to communicate with these messages, revisit library option)
	 * @param clazz the concrete class that implements {@link IEvent}
	 * @param code a globally unique integer that identifies this concrete class
	 * @return false if the type code was already registered
	 */
	boolean registerTypeCode(Class<? extends IEvent> clazz, int code);

}
