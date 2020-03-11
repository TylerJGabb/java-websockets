package com.gabb.sb.architecture;

import ch.qos.logback.classic.Level;
import com.gabb.sb.architecture.messages.IMessage;
import com.gabb.sb.architecture.messages.MessageWithInteger;
import com.gabb.sb.architecture.messages.MessageWithString;
import com.gabb.sb.architecture.messages.StartTestMessage;
import com.gabb.sb.architecture.messages.TestRunnerFinished;
import com.gabb.sb.architecture.messages.publish.AbstractMessagePublisher;
import com.gabb.sb.architecture.messages.publish.IMessagePublisher;
import com.gabb.sb.architecture.messages.subscribe.AbstractMessageSubscriber;
import com.gabb.sb.architecture.resolver.AbstractMessageResolver;
import com.gabb.sb.architecture.resolver.IMessageResolver;
import com.gabb.sb.architecture.resolver.strategies.JsonMessageResolveStrategy;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Util {
	
	public static void configureLoggersProgrammatically(Level aLevel) {
		List<String> loggersNamedByPackage = Arrays.asList(
				"io.netty.handler.codec.http.websocketx",
				"io.netty.buffer",
				"io.netty.util.internal",
				"io.netty.util",
				"io.netty.channel",
				"io.netty.resolver.dns"
		);

		for (String mPackage : loggersNamedByPackage) {
			((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(mPackage)).setLevel(aLevel);
		}
	}


	/**
	 * Builds a resolver using the {@link JsonMessageResolveStrategy} and registered with
	 * {@link MessageWithInteger} and {@link MessageWithString}
	 */
	public static IMessageResolver testJsonResolver() {
		IMessageResolver mResolver = AbstractMessageResolver.resolver();
		mResolver.setStrategy(new JsonMessageResolveStrategy());
		mResolver.registerTypeCode(MessageWithInteger.class, 0x01);
		mResolver.registerTypeCode(MessageWithString.class, 0x02);
		mResolver.registerTypeCode(TestRunnerFinished.class, 0x03);
		mResolver.registerTypeCode(StartTestMessage.class, 0x04);
		return mResolver;
	}

	/**
	 * Builds a publisher with subscribers to {@link MessageWithInteger} and {@link MessageWithString}
	 * @return
	 */
	public static IMessagePublisher publisher() {
		IMessagePublisher publisher = new AbstractMessagePublisher() {};
		publisher.addSubscriber(new AbstractMessageSubscriber<>(MessageWithString.class) {

			@Override
			public void process(MessageWithString message) {
				System.out.println("Got MessageWithString containing " + message.getString());
			}
		});

		publisher.addSubscriber(new AbstractMessageSubscriber<>(MessageWithInteger.class) {

			@Override
			public void process(MessageWithInteger message) {
				System.out.println("Got MessageWithInteger containing " + message.getTheInteger());
			}
		});
		
		publisher.addSubscriber(new AbstractMessageSubscriber<>(TestRunnerFinished.class) {

			@Override
			public void process(TestRunnerFinished message) {
				System.out.println("Got " + message);
			}
		});
		
		publisher.addSubscriber(new AbstractMessageSubscriber<>(StartTestMessage.class) {
			@Override
			public void process(StartTestMessage message) {
				//TODO: THIS IS NOT THREAD SAFE!!!!!
				System.out.println("MOCK START TEST " + message.buildPath + " " + message.cucumberArgs);
			}
		});
		
		return publisher;
	}
}
