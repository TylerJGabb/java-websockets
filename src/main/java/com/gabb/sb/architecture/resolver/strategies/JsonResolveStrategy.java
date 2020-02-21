package com.gabb.sb.architecture.resolver.strategies;

import com.gabb.sb.architecture.payloads.IPayload;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

public class JsonResolveStrategy implements IPayloadResolveStrategy {

	@Override
	public void serialize(Buffer writeTo, IPayload payload) {
		writeTo.appendString(Json.encode(payload));
	}

	@Override
	public IPayload deSerialize(Buffer readFrom, Class<? extends  IPayload> clazz) {
		return Json.decodeValue(readFrom, clazz);
	}
	
}
