package com.gabb.sb.architecture.resolver;

import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.resolver.strategies.IMessageResolveStrategy;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public abstract class AbstractMessageResolver implements IMessageResolver {

	public static final int BYTES_PER_INT = Integer.SIZE / Byte.SIZE;
	private HashMap<Integer, Class<? extends IMessage>> oCodeToTypeMap;
	private HashMap<Class<? extends IMessage>, Integer> oTypeToCodeMap;
	private IMessageResolveStrategy oStrategy;
	private final Logger oLogger;
	
	public static IMessageResolver resolver(){
		return new AbstractMessageResolver(){};
	}
	

	public AbstractMessageResolver() {
		oLogger = LoggerFactory.getLogger(this.getClass());
		oCodeToTypeMap = new HashMap<>();
		oTypeToCodeMap = new HashMap<>();
	}
	
	@Override
	public void setStrategy(IMessageResolveStrategy strategy) {
		oStrategy = strategy;
	}

	@Override
	public IMessage resolve(Buffer buf) {
		try {
			//what if can't read int?
			oLogger.info("Resolving raw buffer '{}'", buf);
			int mTypeCode = buf.getInt(0);
			Class<? extends IMessage> clazz = oCodeToTypeMap.get(mTypeCode);
			if(clazz == null) {
				oLogger.warn("Attempted to resolve unregistered type code {}", mTypeCode);
				return null; //TODO: i think returning null hides, things, should throw exception?
			}
			Buffer payloadSection = buf.getBuffer(BYTES_PER_INT, buf.length());
			return oStrategy.deSerialize(payloadSection, clazz); //exception might be thrown here too... runtime...
			//maybe good practice here would be to catch exception, log it, then return null.
			//the returned null could be a signal to the client that something went wrong,
			//and the exception would appear in logs...
		} catch (Exception e) {
			oLogger.error("Unhandled exception while resolving '{}'", buf, e);
			return null;
		}
	}

	@Override
	public Buffer resolve(IMessage message) {
		Integer typeCode = oTypeToCodeMap.get(message.getClass());
		if(typeCode == null) {
			oLogger.warn("Attempted to resolve unregistered message {}", message.getClass());
			return null;
		}
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
