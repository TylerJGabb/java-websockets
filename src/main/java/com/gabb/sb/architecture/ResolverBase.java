package com.gabb.sb.architecture;

import com.gabb.sb.architecture.IPayload;
import com.gabb.sb.architecture.IResolver;
import io.vertx.core.buffer.Buffer;

import java.util.HashMap;

public abstract class ResolverBase implements IResolver {
	
	protected HashMap<Integer, Class<? extends IPayload>> oTypeMap;

	public ResolverBase() {
		oTypeMap = new HashMap<>();
	}

	@Override
	public boolean registerTypeCode(Class<? extends IPayload> clazz, int code) {
		return oTypeMap.put(code, clazz) != null;
	}

	@Override
	public abstract IPayload resolve(Buffer buf);
}
