package com.gabb.sb.architecture;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ResourcePool {

	private static Logger oLogger;
	private final ExecutorService oNewResouceExecutor;
	private List<ServerTestRunner> oTestRunners;
	private final Object mutex;
	private static ResourcePool oInstance;

	public static ResourcePool getInstance() {
		if (oInstance == null) {
			synchronized (ResourcePool.class){
				if(oInstance == null){
					oInstance = new ResourcePool();
				}
			}
		}
		return oInstance;
	}

	private ResourcePool() {
		oLogger = LoggerFactory.getLogger(ResourcePool.class);
		mutex = new Object();
		oNewResouceExecutor = Executors.newSingleThreadExecutor();
		oTestRunners = new ArrayList<>();
	}


	public void add(final ServerWebSocket sock) {
		oNewResouceExecutor.submit(() -> {
			var headers = sock.headers();
			var tags = headers.get("bench.tags");
			var client = new ServerTestRunner(sock, tags);
			sock.closeHandler(__ -> {
				synchronized (mutex) {
					oTestRunners.remove(client); //lock
				}
				//this makes me want to call is event bus...
				oLogger.info("MOCK: Insert into main message queue that the run assigned to {} is to be deleted", client);
				oLogger.info("Client {} closed and has been removed from the resource pool", client);
			});
			sock.exceptionHandler(ex -> oLogger.error("Unhandled exception in socket {}", client, ex));
			synchronized (mutex) {
				oTestRunners.add(client);
			}
			oLogger.info("Client {} connected and is now available in the resource pool", client);
		});
	}

	public void visitAll(Consumer<List<ServerTestRunner>> listConsumer, Predicate<ServerTestRunner> filter){
		List<ServerTestRunner> serverTestRunners;
		synchronized (mutex) {
			serverTestRunners = oTestRunners.stream().filter(filter).collect(Collectors.toList());
		}
		listConsumer.accept(serverTestRunners);
	}

	public void terminate(String runnerSocketAddress) {
		for(var runner : oTestRunners){
			if(runner.getAddress().toString().equalsIgnoreCase(runnerSocketAddress)){
				runner.stopTest();
				return;
			}
		}
	}

	/**
	 * Do not allow access of resources if any are being added/removed, block the visitor (sorry I don't know what else to call it)
	 * until  the add/remove operation is performed
	 * 
	 * Conversley, do not allow adding/removing while the visitor is visiting. 
	 * 
	 * The problem does not come from adding, but from removing. If a visitor is currently trying to access a resource
	 * while simultaneously a resource is being removed from the pool (due to closing or some exception handle...)
	 * then there could be some concurrency issues.
	 * 
	 * Even If we block, and synchronize these events, it doesnt stop the problem of a socket possibly closing in the middle
	 * of sending something through. Therefore, I think there needs to be some sort of error handling. 
	 * 
	 * SHould the client (meaning the visitor) be responsible for this handling? Can it all be handled internally?
	 * 
	 */

}
