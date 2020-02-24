package com.gabb.sb.architecture.resolver;

import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.resolver.strategies.IMessageResolveStrategy;
import io.vertx.core.buffer.Buffer;

import java.util.HashMap;

public abstract class AbstractMessageResolver implements IMessageResolver {
	
	private HashMap<Integer, Class<? extends IMessage>> oCodeToTypeMap;
	private HashMap<Class<? extends IMessage>, Integer> oTypeToCodeMap;
	private IMessageResolveStrategy oStrategy;
	
	public static IMessageResolver resolver(){
		return new AbstractMessageResolver(){};
	}
	

	public AbstractMessageResolver() {
		oCodeToTypeMap = new HashMap<>();
		oTypeToCodeMap = new HashMap<>();
	}
	
	@Override
	public void setStrategy(IMessageResolveStrategy strategy) {
		oStrategy = strategy;
	}

	@Override
	public IMessage resolve(Buffer buf) {
		int mTypeCode = buf.getInt(0);
		Class<? extends IMessage> clazz = oCodeToTypeMap.get(mTypeCode);
		if(clazz == null) return null;
		Buffer payloadSection = buf.getBuffer(Integer.SIZE / Byte.SIZE, buf.length());
		return oStrategy.deSerialize(payloadSection, clazz);
	}

	@Override
	public Buffer resolve(IMessage message) {
		Integer typeCode = oTypeToCodeMap.get(message.getClass());
		if(typeCode == null) return null;
		Buffer writeTo = Buffer.buffer();
		writeTo.appendInt(typeCode);
		oStrategy.serialize(writeTo, message);
		return writeTo;
	}

	@Override
	public boolean registerTypeCode(Class<? extends IMessage> clazz, int code) {
		if(oCodeToTypeMap.containsKey(code)) return false;
		oCodeToTypeMap.put(code, clazz);
		oTypeToCodeMap.put(clazz, code);
		return true;
	}

}
