package com.gabb.sb.architecture.messages.subscribe;

import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.messages.publish.IMessagePublisher;

public interface IMessageSubscriber<T extends IMessage> {

	void process(T message);

	/**
	 * Returns the runtime type that this {@link IMessageSubscriber} is subscribed to.
	 * Used when publishing an {@link IMessage} to any subscribers in
	 * {@link IMessagePublisher#publish(IMessage)}
	 */
	Class<T> subscription();
	
}
