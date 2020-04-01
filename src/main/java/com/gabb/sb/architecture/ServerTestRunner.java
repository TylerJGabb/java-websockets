package com.gabb.sb.architecture;

import com.gabb.sb.Guarded;
import com.gabb.sb.architecture.events.bus.ConcurrentEventBus;
import com.gabb.sb.architecture.events.bus.IEventBus;
import com.gabb.sb.architecture.events.concretes.StartRunEvent;
import com.gabb.sb.architecture.events.concretes.StopTestEvent;
import com.gabb.sb.architecture.events.concretes.TestRunnerFinishedEvent;
import com.gabb.sb.architecture.events.resolver.IEventResolver;
import com.gabb.sb.spring.entities.Run;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Represents a remote resource on server-side
 */
public class ServerTestRunner extends Guarded {

	private final Logger oLogger;
	private final ServerWebSocket oSock;
	private final IEventResolver oResolver;
	private final IEventBus oLocalEventBus;
	private final IEventBus oMainEventBus;
	private volatile Status oStatus;
	private volatile Integer oRunId;
	private List<String> oBenchTags;

	public ServerTestRunner(ServerWebSocket aSock, String rawTags) {
		// this class needs to be able to resolve messages.... Where is it going to get that resolver?
		// it can't be shared, because it would cause multi-threading issues
		// a new one would need to be created, but where do we get type codes?
		// for now, let some util class take care of it...
		oSock = aSock;
		oLogger = LoggerFactory.getLogger("ServerTestRunner-" + aSock.remoteAddress());
		oResolver = Util.testJsonResolver();
		oMainEventBus = DatabaseChangingEventBus.getInstance();
		oSock.handler(this::handle);
		oLocalEventBus = ConcurrentEventBus.builder()
				.addListener(TestRunnerFinishedEvent.class, this::onFinished)
				.build();
		oBenchTags = rawTags == null || rawTags.strip().isEmpty()
				? Collections.emptyList()
				: Arrays.asList(rawTags.split(","));
		oStatus = Status.IDLE;
	}

	private void onFinished(TestRunnerFinishedEvent trf) {
		if (!oRunId.equals(trf.runId)) {
			//when can this happen?
			oLogger.warn("Received TestRunnerFinishedMessaged with out of date runId. Ignoring...");
		} else {
			oLogger.info("TestRunnerFinished with runId: {}", trf.runId);
			oStatus = Status.IDLE;
			oRunId = null;
		}
		oMainEventBus.push(trf);
	}
	
	private void handle(Buffer aBuf) {
		oLocalEventBus.push(oResolver.resolve(aBuf));
	}
	
	public boolean startTestReturnSuccessful(Run aRun){
		//build message here from run to send through socket.
		//for now send hard coded
		oStatus = Status.RUNNING;
		oRunId = aRun.getId();
		var event = new StartRunEvent("/home/mms/foo/bar/build.zip", "zip zap zop", aRun.getId());
		try {
			oSock.writeBinaryMessage(oResolver.resolve(event));
			oLogger.info("Started run {} on {}", oRunId, oSock.remoteAddress());
		} catch (IllegalStateException socketException){
			oLogger.error("Error when starting test for run {}", oRunId, socketException);
			oStatus = Status.ERROR;
			return false;
		}
		return true;
	}
	
	public void stopTest(){
		try {
			oSock.writeBinaryMessage(oResolver.resolve(new StopTestEvent()));
			oLogger.info("Run {} on {} has been terminated", oRunId, oSock.remoteAddress());
			oRunId = null;
			oStatus = Status.IDLE;
		} catch (IllegalStateException socketException){
			oLogger.error("Error when trying to stop currently executing test", socketException);
			oStatus = Status.ERROR;
		}

	}

	public List<String> getBenchTags() {
		return new ArrayList<>(oBenchTags);
	}

	public Status getStatus() {
		return oStatus;
	}

	public SocketAddress getAddress(){
		return oSock.remoteAddress();
	}

	@Override
	public String toString() {
		return "ServerTestRunner{" +
				"oSock=" + oSock +
				", oStatus='" + oStatus + '\'' +
				", oBenchTags=" + oBenchTags +
				'}';
	}

	//used in debugging to force connection resets when trying to test robustness
	public void close() {
		oSock.close();
	}

	public boolean isIdle() {
		return oStatus.equals(Status.IDLE);
	}

	public Integer getRunId() {
		return oRunId;
	}
}
