package com.gabb.sb.architecture;

import io.vertx.core.buffer.Buffer;

import java.util.HashMap;

public abstract class ResolverBase implements IResolver {
	
	HashMap<Integer, Class<? extends IPayload>> oTypeMap;

	public ResolverBase() {
		oTypeMap = new HashMap<>();
	}

	@Override
	public boolean registerType(Class<? extends IPayload> clazz) {
		return oTypeMap.put(clazz.hashCode(), clazz) == null;
	}

	@Override
	public abstract IPayload resolve(Buffer buf);
}
