package com.gabb.sb.architecture.events.resolver.strategies;

import com.gabb.sb.architecture.events.IEvent;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

public class JsonEventResolveStrategy implements IEventResolveStrategy {

	@Override
	public void serialize(Buffer writeTo, IEvent message) {
		writeTo.appendString(Json.encode(message));
	}

	@Override
	public IEvent deSerialize(Buffer readFrom, Class<? extends IEvent> clazz) {
		return Json.decodeValue(readFrom, clazz);
	}
	
}
