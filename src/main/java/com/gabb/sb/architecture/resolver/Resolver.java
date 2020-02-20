package com.gabb.sb.architecture.resolver;

import com.gabb.sb.architecture.IPayload;
import com.gabb.sb.architecture.resolver.strategies.IPayloadResolveStrategy;
import io.vertx.core.buffer.Buffer;

import java.util.HashMap;

public abstract class Resolver implements IResolver {
	
	private HashMap<Integer, Class<? extends IPayload>> oCodeToTypeMap;
	private HashMap<Class<? extends IPayload>, Integer> oTypeToCodeMap;
	private IPayloadResolveStrategy oStrategy;
	
	public static IResolver resolver(){
		return new Resolver(){};
	}
	

	public Resolver() {
		oCodeToTypeMap = new HashMap<>();
		oTypeToCodeMap = new HashMap<>();
	}
	
	@Override
	public void setStrategy(IPayloadResolveStrategy strategy) {
		oStrategy = strategy;
	}

	@Override
	public IPayload resolve(Buffer buf) {
		Class<? extends IPayload> clazz = oCodeToTypeMap.get(buf.getInt(0));
		if(clazz != null){
			Buffer payloadSection = buf.getBuffer(Integer.SIZE / Byte.SIZE, buf.length());
			return oStrategy.deSerialize(payloadSection, clazz);
		} else {
			return null; //TODO: Throw unresolvable exception, maybe log something!
		}
	}

	@Override
	public Buffer resolve(IPayload payload) {
		Integer typeCode = oTypeToCodeMap.get(payload.getClass());
		if(typeCode != null){
			Buffer writeTo = Buffer.buffer();
			writeTo.appendInt(typeCode);
			oStrategy.serialize(writeTo, payload);
			return writeTo;
		} else {
			return null; //TODO: Throw UnresolvableException. Maybe log something too!!
		}
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
