package com.gabb.sb;

import com.gabb.sb.architecture.DatabaseChangingEventBus;
import com.gabb.sb.architecture.ServerTestRunner;
import com.gabb.sb.architecture.events.concretes.DeleteRunEvent;
import io.vertx.core.http.ServerWebSocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.gabb.sb.Loggers.RESOURCE_POOL_LOGGER;

public class ResourcePool implements IResourceAcceptsVisitor {

    public static final String TEST_RUNNER_CONNECT_FMT = "TestRunner {} has connected and is now available in the resource pool";
    public static final String UNHANDLED_EX_FMT = "Unhandled socket exception occurred for TestRunner {}";
    public static final String CONNECT_CLOSED_FMT = "TestRunner {} connection has closed. Removing from ResourcePool";
    public static final String ACTIVE_RUN_QUEUE_DELETION_FMT = "TestRunner {} was running {} when connection closed. Run deletion queued";
    private static ResourcePool oInstance;
    private final ExecutorService oExecutor;
    private final List<IResourceAcceptsVisitor> oResources;

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
        oResources = Collections.synchronizedList(new ArrayList<>());
        oExecutor = Executors.newSingleThreadExecutor();
    }

    private void onSocketException(Throwable aThrowable, ServerTestRunner aServerTestRunner){
        RESOURCE_POOL_LOGGER.error(UNHANDLED_EX_FMT, aServerTestRunner, aThrowable);
    }

    private void onConnectionClosed(ServerTestRunner aServerTestRunner) {
        RESOURCE_POOL_LOGGER.info(CONNECT_CLOSED_FMT, aServerTestRunner);
        oResources.remove(aServerTestRunner);
        var runId = aServerTestRunner.getRunId();
        if (runId == null) return;
        DatabaseChangingEventBus.getInstance().push(new DeleteRunEvent(runId));
        RESOURCE_POOL_LOGGER.info(ACTIVE_RUN_QUEUE_DELETION_FMT, aServerTestRunner, runId);
    }

    public void add(ServerWebSocket aSocket){
        oExecutor.submit(() -> {
            var serverTestRunner = new ServerTestRunner(aSocket);
            aSocket.closeHandler(__ -> onConnectionClosed(serverTestRunner));
            aSocket.exceptionHandler(ex -> onSocketException(ex, serverTestRunner));
            oResources.add(serverTestRunner);
            RESOURCE_POOL_LOGGER.info(TEST_RUNNER_CONNECT_FMT, serverTestRunner);
        });
    }

    @Override
    public boolean accept(IResourceVisitor visitor) {
        synchronized (oResources) {
            for (IResourceAcceptsVisitor resource : oResources) {
                if (resource.accept(visitor)) return true;
            }
        }
        return false;
    }
}
