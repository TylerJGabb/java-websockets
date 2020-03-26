package com.gabb.sb.architecture;

import com.gabb.sb.architecture.events.bus.EventBus;
import com.gabb.sb.architecture.events.bus.IEventBus;
import com.gabb.sb.architecture.events.concretes.StartTestEvent;
import com.gabb.sb.architecture.events.concretes.TestRunnerFinishedEvent;
import com.gabb.sb.architecture.events.resolver.IEventResolver;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents a remote resource on server-side
 */
public class ServerTestRunner {

	private final Logger oLogger;
	private final ServerWebSocket oSock;
	private final IEventResolver oResolver;
	private final IEventBus oEventBus;
	private volatile String oStatus;
	private Integer oRunId;

	public ServerTestRunner(ServerWebSocket aSock) {
		// this class needs to be able to resolve messages.... Where is it going to get that resolver?
		// it can't be shared, because it would cause multi-threading issues
		// a new one would need to be created, but where do we get type codes?
		// for now, let some util class take care of it...
		oStatus = "WAITING FOR STATUS";
		oLogger = LoggerFactory.getLogger("ServerTestRunner-" + aSock.remoteAddress());
		oResolver = Util.testJsonResolver();
		oEventBus = getEventBus();
		oSock = aSock;
		aSock.handler(this::handle);
		oStatus = "IDLE";
	}

	private IEventBus getEventBus() {
		return EventBus.builder()
				.addListener(TestRunnerFinishedEvent.class, trf -> {
					if (!oRunId.equals(trf.runId)) {
						oLogger.warn("Received TestRunnerFinishedMessaged with out of date runId. Ignoring...");
					} else {
						oLogger.info("TestRunnerFinished with runId: {}", trf.runId);
						oStatus = "IDLE";
						oRunId = null;
					}
				}).build();
	}

	private void handle(Buffer aBuf) {
		//how are we going to get access to main message queue? composition?... decide later
		var msg = oResolver.resolve(aBuf);
		oEventBus.push(msg);
		oLogger.info("MOCK: Putting Resolved Message {} into EventBus", msg.getClass().getSimpleName());
		
	}
	
	public void startTest(Run aRun){
		//build message here from run to send through socket.
		//for now send hard coded
		oStatus = "RUNNING";
		oRunId = aRun.getId();
		var msg = new StartTestEvent("/home/mms/foo/bar/build.zip", "zip zap zop", aRun.getId());
		oSock.writeBinaryMessage(oResolver.resolve(msg));
		oLogger.info("MOCK: Started Run {}", aRun.getId());
	}

	public String getStatus() {
		return oStatus;
	}

	@Override
	public String toString() {
		return "ServerTestRunner{" + "oSock=" + oSock + ", oStatus='" + oStatus + '\'' + '}';
	}
}
