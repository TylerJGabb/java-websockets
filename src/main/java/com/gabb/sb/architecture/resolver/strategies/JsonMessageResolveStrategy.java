package com.gabb.sb.architecture.resolver.strategies;

import com.gabb.sb.architecture.messages.IMessage;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

public class JsonMessageResolveStrategy implements IMessageResolveStrategy {

	@Override
	public void serialize(Buffer writeTo, IMessage message) {
		writeTo.appendString(Json.encode(message));
	}

	@Override
	public IMessage deSerialize(Buffer readFrom, Class<? extends IMessage> clazz) {
		return Json.decodeValue(readFrom, clazz);
	}
	
}
