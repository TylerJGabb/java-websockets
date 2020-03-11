package com.gabb.sb.architecture;

import com.gabb.sb.architecture.messages.publish.IMessagePublisher;
import com.gabb.sb.architecture.resolver.IMessageResolver;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ResourcePool {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourcePool.class);
	private List<ServerTestRunner> oTestRunners;

	public ResourcePool() {
		oTestRunners = new ArrayList<>();
	}

	public void add(ServerWebSocket sock){
		var client = new ServerTestRunner(sock);
		var address = sock.remoteAddress();
		sock.closeHandler(__ -> {
			oTestRunners.remove(client);
			LOGGER.info("WebSocket {} closed and has been removed from the resource pool", address);
		});
		sock.exceptionHandler(ex -> LOGGER.error("Unhandled exception in socket {}", address, ex));
		oTestRunners.add(client);
		LOGGER.info("WebSocket {} connected and is now available in the resource pool", address);
	}

	/**
	 * Allows thread-safe access of clients to perform opertaions like starting
	 * tests, sending messages, and turning features on/off
	 * //TODO: THIS IS NOT THE VISITOR PATTERN!
	 * //TODO: THIS IS NOT THREAD SAFE!
	 */
	public void visit(Function<ServerTestRunner, Boolean> apply){
		for (ServerTestRunner serverTestRunner : oTestRunners) {
			if(apply.apply(serverTestRunner)) return;
		}
	}
	
}
