package com.gabb.sb.architecture.resolver;

import com.gabb.sb.architecture.payloads.IPayload;
import com.gabb.sb.architecture.resolver.strategies.IPayloadResolveStrategy;
import io.vertx.core.buffer.Buffer;

import java.util.HashMap;

public abstract class AbstractResolver implements IResolver {
	
	private HashMap<Integer, Class<? extends IPayload>> oCodeToTypeMap;
	private HashMap<Class<? extends IPayload>, Integer> oTypeToCodeMap;
	private IPayloadResolveStrategy oStrategy;
	
	public static IResolver resolver(){
		return new AbstractResolver(){};
	}
	

	public AbstractResolver() {
		oCodeToTypeMap = new HashMap<>();
		oTypeToCodeMap = new HashMap<>();
	}
	
	@Override
	public void setStrategy(IPayloadResolveStrategy strategy) {
		oStrategy = strategy;
	}

	@Override
	public IPayload resolve(Buffer buf) {
		int mTypeCode = buf.getInt(0);
		Class<? extends IPayload> clazz = oCodeToTypeMap.get(mTypeCode);
		if(clazz == null) return null;
		Buffer payloadSection = buf.getBuffer(Integer.SIZE / Byte.SIZE, buf.length());
		return oStrategy.deSerialize(payloadSection, clazz);
	}

	@Override
	public Buffer resolve(IPayload payload) {
		Integer typeCode = oTypeToCodeMap.get(payload.getClass());
		if(typeCode == null) return null;
		Buffer writeTo = Buffer.buffer();
		writeTo.appendInt(typeCode);
		oStrategy.serialize(writeTo, payload);
		return writeTo;
	}

	@Override
	public boolean registerTypeCode(Class<? extends IPayload> clazz, int code) {
		if(oCodeToTypeMap.containsKey(code)) return false;
		oCodeToTypeMap.put(code, clazz);
		oTypeToCodeMap.put(clazz, code);
		return true;
	}

	/**
	 * Returns the value to which the specified key is mapped,
	 * 	or {@code null} if this map contains no mapping for the key.
	 */
	protected Class<? extends  IPayload> lookupTypeFromCode(int code){
		return oCodeToTypeMap.get(code);
	}
	
}
