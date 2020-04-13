package com.gabb.sb;

import com.gabb.sb.architecture.ServerTestRunner;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.gabb.sb.Loggers.RESOURCE_POOL_LOGGER;

public class ResourcePool implements IResourceAcceptsVisitor, Handler<ServerWebSocket> {

    public static final String TEST_RUNNER_CONNECT_FMT = "TestRunner {} has connected and is now available in the resource pool";
    public static final String WEBSOCKET_CONN_RE_ESTABLISHED_FMT = "TestRunner websocket connection re-established from host {}";
    public static final String TEST_RUNNER_INITIAL_CONNECTION_FMT = "TestRunner initial connection from host {}";
    public static final String SECOND_WEBSOCKET_ATTEMPT_IGNORE_FMT = "Host {} attempted to open a second websocket. Ignoring";
    private static ResourcePool oInstance;
    private final ExecutorService oExecutor;
    private final Map<String, IResourceAcceptsVisitor> oResources;

    public static ResourcePool getInstance(){
        if(oInstance == null){
            synchronized (ResourcePool.class){
                if(oInstance == null){
                    oInstance = new ResourcePool();
                }
            }
        }
        return oInstance;
    }

    private ResourcePool() {
        oResources = Collections.synchronizedMap(new HashMap<>());
        oExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void handle(ServerWebSocket aSocket){
        oExecutor.submit(() -> {
            String host = aSocket.remoteAddress().host();
            ServerTestRunner serverTestRunner = (ServerTestRunner) oResources.get(host);
            if(serverTestRunner == null){
                RESOURCE_POOL_LOGGER.info(TEST_RUNNER_INITIAL_CONNECTION_FMT, host);
                serverTestRunner = new ServerTestRunner(host);
                oResources.put(host, serverTestRunner);
            } else {
                if(serverTestRunner.hasActiveSocket()){
                    RESOURCE_POOL_LOGGER.error(SECOND_WEBSOCKET_ATTEMPT_IGNORE_FMT, host);
                    aSocket.reject();
                    return;
                }
                RESOURCE_POOL_LOGGER.info(WEBSOCKET_CONN_RE_ESTABLISHED_FMT, host);
            }
            serverTestRunner.setSocket(aSocket);
            RESOURCE_POOL_LOGGER.info(TEST_RUNNER_CONNECT_FMT, serverTestRunner);
        });
    }

    @Override
    public boolean accept(IResourceVisitor visitor) {
        synchronized (oResources) {
            for (IResourceAcceptsVisitor resource : oResources.values()) {
                if (resource.accept(visitor)) return true;
            }
        }
        return false;
    }
}
