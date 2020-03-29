package com.gabb.sb.architecture;

import com.gabb.sb.architecture.events.bus.ConcurrentEventBus;
import com.gabb.sb.architecture.events.bus.IEventBus;
import com.gabb.sb.architecture.events.concretes.StartRunEvent;
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
	private final IEventBus oLocalEventBus;
	private final IEventBus oMainEventBus;
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
		oLocalEventBus = getLocalEventBus();
		oMainEventBus = DatabaseChangingEventBus.getInstance();
		oSock = aSock;
		aSock.handler(this::handle);
		oStatus = "IDLE";
	}

	private void onFinished(TestRunnerFinishedEvent trf) {
		if (!oRunId.equals(trf.runId)) {
			//when can this happen?
			oLogger.warn("Received TestRunnerFinishedMessaged with out of date runId. Ignoring...");
		} else {
			oLogger.info("TestRunnerFinished with runId: {}", trf.runId);
			oStatus = "IDLE";
			oRunId = null;
		}
	}
	
	private IEventBus getLocalEventBus() {
		return ConcurrentEventBus.builder()
				.addListener(TestRunnerFinishedEvent.class, this::onFinished)
				.build();
	}

	private void handle(Buffer aBuf) {
		//how are we going to get access to main message queue? composition?... decide later
		var event = oResolver.resolve(aBuf);
		oLocalEventBus.push(event);
		oLogger.info("Putting Resolved Event {} into DatabaseChangingEventBus", event.getClass().getSimpleName());
		oMainEventBus.push(event);
	}
	
	public void startTest(Run aRun){
		//build message here from run to send through socket.
		//for now send hard coded
		oStatus = "RUNNING";
		oRunId = aRun.getId();
		var event = new StartRunEvent("/home/mms/foo/bar/build.zip", "zip zap zop", aRun.getId());
		oSock.writeBinaryMessage(oResolver.resolve(event));
		//if successful
		oMainEventBus.push(event);
	}
	
	public void stopTest(){
		// set internal status
		// submit event to MEB for database processing (no?)
	}

	public String getStatus() {
		return oStatus;
	}

	@Override
	public String toString() {
		return "ServerTestRunner{" + "oSock=" + oSock + ", oStatus='" + oStatus + '\'' + '}';
	}
}
