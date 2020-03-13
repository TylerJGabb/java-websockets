package com.gabb.sb.architecture;

import com.gabb.sb.architecture.messages.StartTestMessage;
import com.gabb.sb.architecture.messages.TestRunnerFinishedMessage;
import com.gabb.sb.architecture.messages.publish.AbstractMessagePublisher;
import com.gabb.sb.architecture.messages.publish.IMessagePublisher;
import com.gabb.sb.architecture.resolver.IMessageResolver;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


/**
 * Represents a remote resource on server-side
 */
public class ServerTestRunner {

	private final Logger oLogger;
	private final ServerWebSocket oSock;
	private final IMessageResolver oResolver;
	private final IMessagePublisher oPublisher;
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
		oPublisher = getPublisher();
		oSock = aSock;
		aSock.handler(this::handle);
		oStatus = "IDLE";
	}

	private IMessagePublisher getPublisher() {
		return AbstractMessagePublisher.builder()
				.addSubscriber(TestRunnerFinishedMessage.class, trf -> {
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
		oPublisher.publish(msg);
		oLogger.info("MOCK: Putting Resolved Message {} into EventBus", msg.getClass().getSimpleName());
		
	}
	
	public void startTest(Run aRun){
		//build message here from run to send through socket.
		//for now send hard coded
		oRunId = aRun.getId();
		oStatus = "RUNNING";
		var msg = new StartTestMessage("/home/mms/foo/bar/build.zip", "zip zap zop", aRun.getId());
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
