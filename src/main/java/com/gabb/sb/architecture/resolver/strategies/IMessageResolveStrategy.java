package com.gabb.sb.architecture.resolver.strategies;

import com.gabb.sb.architecture.messages.IMessage;
import io.vertx.core.buffer.Buffer;

public interface IMessageResolveStrategy {

	/**
	 * Resolves a {@link IMessage} into an {@link IMessage}
	 * @param writeTo the serialized contents of the IMessage shall be appended to this buffer
	 * @param
	 */
	void serialize(Buffer writeTo, IMessage message);

	/**
	 * Resolves a buffer and class type into an IMessage
	 */
	IMessage deSerialize(Buffer readFrom, Class<? extends IMessage> clazz);
	
}
