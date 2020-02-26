package com.gabb.sb.architecture.messages.processing;

import com.gabb.sb.architecture.messages.IMessage;
import com.google.common.reflect.TypeToken;

public abstract class AbstractMessageProcessor<T extends IMessage> implements IMessageProcessor<T> {

	/**
	 * {@link TypeToken} is written by google and is a way to capture the runtime type of T
	 * despite type erasure. Its in @Beta phase, and is considered unstable...
	 *
	 * if I don't want to use this then I'd have to instantiate a message processor
	 * like so:
	 *
	 * new AbstractMessageProcessor<Message>(Message.class) which I think is
	 * messy and redundant...
	 *
	 * This also opens up the door to user error. i.e.
	 * new AbstractMessageProcessor<Foo>(Bar.class);
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
