package com.gabb.sb.architecture;

import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class ResourcePool {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourcePool.class);
	private final ExecutorService oNewResouceExecutor;
	private List<ServerTestRunner> oTestRunners;
	private final ReentrantLock oCollectionLock;
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
		oCollectionLock = new ReentrantLock(true);
		oNewResouceExecutor = Executors.newSingleThreadExecutor();
		oTestRunners = new ArrayList<>();
	}


	public void add(ServerWebSocket sock) {
		var client = new ServerTestRunner(sock);
		sock.closeHandler(__ -> {
			oTestRunners.remove(client); //lock
			//this makes me want to call is event bus...
			LOGGER.info("MOCK: Insert into main message queue that the run assigned to {} is to be deleted", client);
			LOGGER.info("Client {} closed and has been removed from the resource pool", client);
		});
		sock.exceptionHandler(ex -> LOGGER.error("Unhandled exception in socket {}", client, ex));
		oTestRunners.add(client); //lock
		LOGGER.info("Client {} connected and is now available in the resource pool", client);
	}

	/**
	 * Allows thread-safe access of clients to perform opertaions like starting
	 * tests, sending messages, and turning features on/off
	 * //TODO: THIS IS NOT THE VISITOR PATTERN!
	 * //TODO: THIS IS NOT THREAD SAFE!
	 */
	public void visit(Function<ServerTestRunner, Boolean> apply){
		//lock!
		for (ServerTestRunner serverTestRunner : oTestRunners) {
			if(apply.apply(serverTestRunner)) return;
		}
	}
	
	public boolean allocate(List<IResourceConsumer> consumers){
		//...
		return false;
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
