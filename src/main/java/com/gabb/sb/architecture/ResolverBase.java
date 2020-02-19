package com.gabb.sb.architecture;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.util.HashMap;

public abstract class ResolverBase implements IResolver {
	
	HashMap<Integer, Class<? extends IPayload>> oTypeMap;

	public ResolverBase() {
		oTypeMap = new HashMap<>();
	}
	
	@Override
	public void registerTypeCode(int aCode, Class<? extends IPayload> clazz){
		oTypeMap.put(aCode, clazz);
	}

	@Override
	public IPayload resolve(Buffer buf) {
		int code = buf.getInt(0);
		Class<? extends  IPayload> clazz = oTypeMap.get(code); //TODO: null check here
		Buffer payload = buf.getBuffer(4, buf.length());
		return Json.decodeValue(payload, clazz);
	}
}
