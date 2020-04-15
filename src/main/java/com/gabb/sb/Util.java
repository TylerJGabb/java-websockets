package com.gabb.sb;

import ch.qos.logback.classic.Level;
import com.gabb.sb.events.concretes.DeleteRunEvent;
import com.gabb.sb.events.concretes.StartRunEvent;
import com.gabb.sb.events.concretes.StopTestEvent;
import com.gabb.sb.events.concretes.TestRunnerFinishedEvent;
import com.gabb.sb.events.resolver.AbstractEventResolver;
import com.gabb.sb.events.resolver.IEventResolver;
import com.gabb.sb.events.resolver.strategies.JsonEventResolveStrategy;
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
	 *     {@link TestRunnerFinishedEvent}
	 *     {@link StartRunEvent}
	 *     {@link StopTestEvent}
	 *     {@link DeleteRunEvent}
	 * </pre>
	 */
	public static IEventResolver testJsonResolver() {
		IEventResolver mResolver = AbstractEventResolver.resolver();
		mResolver.setStrategy(new JsonEventResolveStrategy());
		mResolver.registerTypeCode(TestRunnerFinishedEvent.class, 0x03);
		mResolver.registerTypeCode(StartRunEvent.class, 0x04);
		mResolver.registerTypeCode(StopTestEvent.class, 0x05);
		mResolver.registerTypeCode(DeleteRunEvent.class, 0x06);
		return mResolver;
	}
}
