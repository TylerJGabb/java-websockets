package com.gabb.sb.events.resolver.strategies;

import com.gabb.sb.events.IEvent;
import io.vertx.core.buffer.Buffer;

public interface IEventResolveStrategy {

	/**
	 * Resolves a {@link IEvent} into an {@link Buffer}
	 * @param writeTo the serialized contents of the IMessage shall be appended to this buffer
	 * @param
	 */
	void serialize(Buffer writeTo, IEvent message);

	/**
	 * Resolves a {@link Buffer} and class type into an {@link IEvent}
	 */
	IEvent deSerialize(Buffer readFrom, Class<? extends IEvent> clazz);
	
}
