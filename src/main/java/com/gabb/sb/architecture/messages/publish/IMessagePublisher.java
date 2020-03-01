package com.gabb.sb.architecture.messages.publish;

import com.gabb.sb.architecture.messages.subscribe.IMessageSubscriber;
import com.gabb.sb.architecture.messages.IMessage;

/**
 * A class that facilitates processing of messages coming through web sockets by following the pub/sub design
 * pattern.
 */
public interface IMessagePublisher {

	/**
	 * Registers a {@link IMessageSubscriber} to this publisher.
	 */
	void addSubscriber(IMessageSubscriber messageSubscriber);

	/**
	 * Publishes an {@link IMessage} to <strong>any</strong> {@link IMessageSubscriber} that was registered to this publisher via
	 * {@link IMessagePublisher#addSubscriber(IMessageSubscriber)} such that
	 * {@link IMessageSubscriber#subscription()}.equals(message.getClass())
	 * @return false if no subscriber has been registered with this publisher
	 * such that {@link IMessageSubscriber#subscription()#equals}(message.getClass())
	 * otherwise true
	 */
	boolean publish(IMessage message);

}
