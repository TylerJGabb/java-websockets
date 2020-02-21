package com.gabb.sb.architecture.resolver.strategies;

import com.gabb.sb.architecture.payloads.IPayload;
import io.vertx.core.buffer.Buffer;

public interface IPayloadResolveStrategy {

	/**
	 * Resolves a payload writing its contents into the provided Buffer
	 */
	void serialize(Buffer writeTo, IPayload payload);

	/**
	 * Resolves a buffer and class type into an IPayload
	 */
	IPayload deSerialize(Buffer readFrom, Class<? extends  IPayload> clazz);
	
}
