package com.gabb.sb.architecture;

import io.vertx.core.buffer.Buffer;

/**
 * Resolves raw binary data into a Type and Payload
 */
public interface IResolver {
	
	IPayload resolve(Buffer buf);

	void registerTypeCode(int aCode, Class<? extends IPayload> clazz);
	
}
