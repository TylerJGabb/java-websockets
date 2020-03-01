package com.gabb.sb.architecture.messages.subscribe;

import com.gabb.sb.architecture.messages.IMessage;

public abstract class AbstractMessageSubscriber<T extends IMessage> implements IMessageSubscriber<T> {

	private final Class<T> subscription;

	protected AbstractMessageSubscriber(Class<T> subscription){
		//if there was a way to infer this class cleanly I would, but if I don't want to pass
		//this in then I have to infer the type instance by working around type erasure,
		//which causes a lot of compile time warnings. I can use generics here to make sure
		//there isn't a mismatch between subscription class and T at compile time.
		this.subscription = subscription;
	}

	/**
	 * The IMessage extender that this subscriber is subscribed to.
	 * When a message is published, if this method's return value
	 * matches the message instance, {@link AbstractMessageSubscriber#process(IMessage)} will be called
	 * with that message
	 */
	@Override
	public final Class<T> subscription() {
		return subscription;
	}
}
