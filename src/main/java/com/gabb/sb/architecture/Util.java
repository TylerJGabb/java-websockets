package com.gabb.sb.architecture;

import ch.qos.logback.classic.Level;
import com.gabb.sb.architecture.messages.MessageWithInteger;
import com.gabb.sb.architecture.messages.MessageWithString;
import com.gabb.sb.architecture.messages.StartTestMessage;
import com.gabb.sb.architecture.messages.StopTestMessage;
import com.gabb.sb.architecture.messages.TestRunnerFinishedMessage;
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
	 *	Builds a *NEW* Json Resolver that can resolve: 
	 * <pre>
	 *     {@link TestRunnerFinishedMessage}
	 *     {@link StartTestMessage}
	 *     {@link StopTestMessage}
	 * </pre>	
	 */
	public static IMessageResolver testJsonResolver() {
		IMessageResolver mResolver = AbstractMessageResolver.resolver();
		mResolver.setStrategy(new JsonMessageResolveStrategy());
		mResolver.registerTypeCode(TestRunnerFinishedMessage.class, 0x03);
		mResolver.registerTypeCode(StartTestMessage.class, 0x04);
		mResolver.registerTypeCode(StopTestMessage.class, 0x05);
		return mResolver;
	}
}
