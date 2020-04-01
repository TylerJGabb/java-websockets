package com.gabb.sb;

import com.gabb.sb.architecture.DatabaseChangingEventBus;
import com.gabb.sb.architecture.ServerTestRunner;
import com.gabb.sb.architecture.events.concretes.DeleteRunEvent;
import io.vertx.core.http.ServerWebSocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.gabb.sb.Loggers.RESOURCE_POOL_LOGGER;

public class GuardedResourcePool extends GuardedThreadSafeCollection<ServerTestRunner> {

    public static final String TESTRUNNER_CONNECT_FMT = "TestRunner {} has connected and is now available in the resource pool";
    public static final String UNHANDLEX_EX_FMT = "Unhandled socket exception occurred for TestRunner {}";
    public static final String CONNECT_CLOSED_FMT = "TestRunner {} connection has closed. Removing from ResourcePool";
    public static final String ACTIVE_RUN_QUEUE_DELETION_FMT = "TestRunner {} was running {} when connection closed. Run deletion queued";
    private final ExecutorService oExecutor;
    private static GuardedResourcePool oInstance;

    public GuardedResourcePool() {
        super();
        oExecutor = Executors.newSingleThreadExecutor();
    }

    public static GuardedResourcePool getInstance(){
        if(oInstance == null){
            synchronized (GuardedResourcePool.class){
                if(oInstance == null){
                    oInstance = new GuardedResourcePool();
                }
            }
        }
        return oInstance;
    }

    private void onSocketException(Throwable aThrowable, ServerTestRunner aServerTestRunner){
        RESOURCE_POOL_LOGGER.error(UNHANDLEX_EX_FMT, aServerTestRunner, aThrowable);
    }

    private void onConnectionClosed(ServerTestRunner aServerTestRunner) {
        RESOURCE_POOL_LOGGER.info(CONNECT_CLOSED_FMT, aServerTestRunner);
        protectedRemove(aServerTestRunner);
        var runId = aServerTestRunner.getRunId();
        if (runId == null) return;
        DatabaseChangingEventBus.getInstance().push(new DeleteRunEvent(runId));
        RESOURCE_POOL_LOGGER.info(ACTIVE_RUN_QUEUE_DELETION_FMT, aServerTestRunner, runId);
    }

    public void add(final ServerWebSocket aSocket){
        oExecutor.submit(() -> {
            var serverTestRunner = new ServerTestRunner(aSocket);
            aSocket.closeHandler(__ -> onConnectionClosed(serverTestRunner));
            aSocket.exceptionHandler(ex -> onSocketException(ex, serverTestRunner));
            protectedAdd(serverTestRunner);
            RESOURCE_POOL_LOGGER.info(TESTRUNNER_CONNECT_FMT, serverTestRunner);
        });
    }

    public void sendTerminationSignal(String aRunnerSocketRemoteAddressToString){
        //find the runner with same string repr of socket
        findFirstAndConsume(
                runner -> runner.getAddress().toString().equalsIgnoreCase(aRunnerSocketRemoteAddressToString),
                ServerTestRunner::stopTest
        );

    }
}
