package com.gabb.sb;

import ch.qos.logback.classic.Level;
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
}
