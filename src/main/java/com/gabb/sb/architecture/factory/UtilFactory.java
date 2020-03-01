package com.gabb.sb.architecture.factory;

import com.gabb.sb.architecture.messages.MessageWithInteger;
import com.gabb.sb.architecture.messages.MessageWithString;
import com.gabb.sb.architecture.messages.subscribe.AbstractMessageSubscriber;
import com.gabb.sb.architecture.messages.publish.IMessagePublisher;
import com.gabb.sb.architecture.messages.publish.AbstractMessagePublisher;
import com.gabb.sb.architecture.resolver.IMessageResolver;
import com.gabb.sb.architecture.resolver.AbstractMessageResolver;
import com.gabb.sb.architecture.resolver.strategies.JsonMessageResolveStrategy;

public final class UtilFactory {

	/**
	 * Builds a resolver using the {@link JsonMessageResolveStrategy} and registered with
	 * {@link MessageWithInteger} and {@link MessageWithString}
	 */
	public static IMessageResolver testJsonResolver() {
		IMessageResolver mResolver = AbstractMessageResolver.resolver();
		mResolver.setStrategy(new JsonMessageResolveStrategy());
		mResolver.registerTypeCode(MessageWithInteger.class, 0x01);
		mResolver.registerTypeCode(MessageWithString.class, 0x02);
		return mResolver;
	}

	/**
	 * Builds a publisher with subscribers to {@link MessageWithInteger} and {@link MessageWithString}
	 * @return
	 */
	public static IMessagePublisher publisher() {
		IMessagePublisher publisher = new AbstractMessagePublisher() {};
		publisher.addSubscriber(new AbstractMessageSubscriber<MessageWithString>(MessageWithString.class) {

			@Override
			public void process(MessageWithString message) {
				System.out.println("Got MessageWithString containing " + message.getString());
			}
		});

		publisher.addSubscriber(new AbstractMessageSubscriber<MessageWithInteger>(MessageWithInteger.class) {

			@Override
			public void process(MessageWithInteger message) {
				System.out.println("Got MessageWithInteger containing " + message.getTheInteger());
			}
		});
		return publisher;
	}
}
