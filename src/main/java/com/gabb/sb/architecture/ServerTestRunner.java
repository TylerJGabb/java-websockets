package com.gabb.sb.architecture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

import static com.gabb.sb.PropertyKeys.BENCH_TAGS_KEY;
import static com.gabb.sb.PropertyKeys.HUMAN_READABLE_NAME_KEY;


/**
 * Represents a remote resource on server-side
 */
public class ServerTestRunner extends Guarded {

	@JsonIgnore private final Logger oLogger;
	@JsonIgnore private final ServerWebSocket oSock;
	@JsonIgnore private final IEventResolver oResolver;
	@JsonIgnore private final IEventBus oLocalEventBus;
	@JsonIgnore private final IEventBus oMainEventBus;
	@JsonProperty("name") private final String oName;
	@JsonProperty("status") private volatile Status oStatus;
	@JsonProperty("runId") private volatile Integer oRunId;
	@JsonProperty("benchTags") private List<String> oBenchTags;

	public ServerTestRunner(ServerWebSocket aSock) {
		super();
		oSock = aSock;
		oResolver = Util.testJsonResolver();
		oMainEventBus = DatabaseChangingEventBus.getInstance();
		oSock.handler(this::handleIncomingBuffer);
		oLocalEventBus = ConcurrentEventBus.builder()
				.addListener(TestRunnerFinishedEvent.class, this::onFinished)
				.build();

		String rawTags = aSock.headers().get(BENCH_TAGS_KEY);
		oBenchTags = rawTags == null || rawTags.strip().isEmpty()
				? Collections.emptyList()
				: Arrays.asList(rawTags.split(","));

		//each ServerTestRunner should have its own logger, makes it easier to identify behavior in logs
		String humanReadableName = aSock.headers().get(HUMAN_READABLE_NAME_KEY);
		String remoteAddressToString = aSock.remoteAddress().toString();
		oName = humanReadableName == null || humanReadableName.isBlank()
				? remoteAddressToString
				: humanReadableName + ": " + remoteAddressToString;

		oLogger = LoggerFactory.getLogger(oName);
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
	
	private void handleIncomingBuffer(Buffer aBuf) {
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
			oLogger.error("Error when trying to stop current  test", socketException);
			oStatus = Status.ERROR;
		}

	}

	@JsonIgnore
	public List<String> getBenchTags() {
		return new ArrayList<>(oBenchTags);
	}

	@JsonIgnore
	public Status getStatus() {
		return oStatus;
	}

	@JsonIgnore
	public SocketAddress getAddress(){
		return oSock.remoteAddress();
	}

	@Override
	public String toString() {
		return "ServerTestRunner{" +
				"oSock.remoteAddress()=" + oSock.remoteAddress() +
				", oStatus='" + oStatus + '\'' +
				", oBenchTags=" + oBenchTags +
				'}';
	}

	@JsonIgnore
	public boolean isIdle() {
		return oStatus.equals(Status.IDLE);
	}

	@JsonIgnore
	public Integer getRunId() {
		return oRunId;
	}

	@JsonIgnore
	public String getName() {
		return oName;
	}

	//used in debugging to force connection resets when trying to test robustness
	public void close() {
		oSock.close();
	}
}
