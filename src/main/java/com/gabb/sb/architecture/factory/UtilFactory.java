package com.gabb.sb.architecture.factory;

import com.gabb.sb.architecture.listners.AbstractListener;
import com.gabb.sb.architecture.payloads.routing.IPayloadRouter;
import com.gabb.sb.architecture.payloads.routing.AbstractPayloadRouter;
import com.gabb.sb.architecture.resolver.IResolver;
import com.gabb.sb.architecture.resolver.AbstractResolver;
import com.gabb.sb.architecture.payloads.PayloadWithInteger;
import com.gabb.sb.architecture.payloads.PayloadWithString;
import com.gabb.sb.architecture.resolver.strategies.JsonResolveStrategy;

public final class UtilFactory {

	/**
	 * Builds a resolver using the {@link JsonResolveStrategy} and registered with
	 * {@link PayloadWithInteger} and {@link PayloadWithString}
	 */
	public static IResolver testJsonResolver() {
		IResolver mResolver = AbstractResolver.resolver();
		mResolver.setStrategy(new JsonResolveStrategy());
		mResolver.registerTypeCode(PayloadWithInteger.class, 0x01);
		mResolver.registerTypeCode(PayloadWithString.class, 0x02);
		return mResolver;
	}

	public static IPayloadRouter testRouter() {
		IPayloadRouter mPayloadRouter = new AbstractPayloadRouter() {};
		mPayloadRouter.registerListener(new AbstractListener<PayloadWithString>() {

			@Override
			public void consume(PayloadWithString payload) {
				System.out.println("Got PayloadWithString containing " + payload.getString());
			}
		});

		mPayloadRouter.registerListener(new AbstractListener<PayloadWithInteger>() {

			@Override
			public void consume(PayloadWithInteger payload) {
				System.out.println("Got PayloadWithInteger containing " + payload.getTheInteger());
			}
		});
		return mPayloadRouter;
	}
}
