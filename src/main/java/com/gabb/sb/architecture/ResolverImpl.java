package com.gabb.sb.architecture;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

public class ResolverImpl extends ResolverBase {
	
	@Override
	public IPayload resolve(Buffer buf) {
		int code = buf.getInt(0);
		Class<? extends IPayload> clazz = oTypeMap.get(code); //TODO: null check here
		Buffer payload = buf.getBuffer(Integer.SIZE / Byte.SIZE, buf.length());
		return Json.decodeValue(payload, clazz);
	}
}
