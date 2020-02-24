package com.gabb.sb.architecture.messages.processing;

import com.gabb.sb.architecture.messages.IMessage;
import com.google.common.reflect.TypeToken;

public abstract class AbstractMessageProcessor<T extends IMessage> implements IMessageProcessor<T> {

	/**
	 * {@link TypeToken} is written by google and is a way to capture the runtime type of T
	 * despite type erasure. Its in @Beta phase, and is considered unstable...
	 */
	private final TypeToken<T> oToken;

	public AbstractMessageProcessor(){
		oToken = new TypeToken<T>(getClass()){};
	}

	@Override
	public final Class canProcess() {
		return oToken.getRawType();
	}
}
