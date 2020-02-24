package com.gabb.sb.architecture.factory;

import com.gabb.sb.architecture.messages.MessageWithInteger;
import com.gabb.sb.architecture.messages.MessageWithString;
import com.gabb.sb.architecture.messages.processing.AbstractMessageProcessor;
import com.gabb.sb.architecture.messages.dispatching.IMessageDispatcher;
import com.gabb.sb.architecture.messages.dispatching.AbstractMessageDispatcher;
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

	public static IMessageDispatcher testDispatcher() {
		IMessageDispatcher mPayloadRouter = new AbstractMessageDispatcher() {};
		mPayloadRouter.registerPayloadProcessor(new AbstractMessageProcessor<MessageWithString>() {

			@Override
			public void process(MessageWithString payload) {
				System.out.println("Got MessageWithString containing " + payload.getString());
			}
		});

		mPayloadRouter.registerPayloadProcessor(new AbstractMessageProcessor<MessageWithInteger>() {

			@Override
			public void process(MessageWithInteger payload) {
				System.out.println("Got MessageWithInteger containing " + payload.getTheInteger());
			}
		});
		return mPayloadRouter;
	}
}
