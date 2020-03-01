package com.gabb.sb.architecture.messages.publish;


import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.messages.subscribe.IMessageSubscriber;
import com.gabb.sb.architecture.resolver.IMessageResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * When an incoming {@link io.vertx.core.buffer.Buffer} is resolved by an
 * {@link IMessageResolver} into an {@link IMessage},
 * it is then passed to an instance of this class, who then finds an
 * {@link IMessageSubscriber} capable of processing it.
 */

@SuppressWarnings("unchecked")
public abstract class AbstractMessagePublisher implements IMessagePublisher {
	//TODO: make thread safe??

	private HashMap<Class<? extends IMessage>, List<IMessageSubscriber>> oMessageSubscriberMap;

	protected AbstractMessagePublisher() {
		oMessageSubscriberMap = new HashMap<>();
	}

	@Override
	public void addSubscriber(IMessageSubscriber messageSubscriber) {
		Class subscription = messageSubscriber.subscription();
		oMessageSubscriberMap.computeIfAbsent(subscription, __ -> new ArrayList<>());
		oMessageSubscriberMap.get(subscription).add(messageSubscriber);
	}

	@Override
	public boolean publish(IMessage message) {
		List<IMessageSubscriber> subscribers = oMessageSubscriberMap.get(message.getClass());
		if(subscribers == null) return false;
		subscribers.forEach(sub -> sub.process(message));
		return true;
	}
}


