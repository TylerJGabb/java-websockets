package com.gabb.sb.server.resourcepool;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.PropertyKeys;
import com.gabb.sb.Status;
import com.gabb.sb.Util;
import com.gabb.sb.events.bus.ConcurrentEventBus;
import com.gabb.sb.events.bus.IEventBus;
import com.gabb.sb.events.concretes.DeleteRunEvent;
import com.gabb.sb.events.concretes.StartRunEvent;
import com.gabb.sb.events.concretes.StopTestEvent;
import com.gabb.sb.events.concretes.TestRunnerFinishedEvent;
import com.gabb.sb.events.resolver.IEventResolver;
import com.gabb.sb.server.DatabaseChangingEventBus;
import com.gabb.sb.server.entities.Run;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Represents a remote resource on server-side
 */
@JsonAutoDetect(
		isGetterVisibility = JsonAutoDetect.Visibility.NONE, // 'is' like 'isTerminated'
		getterVisibility = JsonAutoDetect.Visibility.NONE,  //  regular getters
		fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ServerTestRunner implements IResourceAcceptsVisitor {

	public static final String ACTIVE_RUN_QUEUE_DELETION_FMT = "Websocket closed when running {}. Run deletion queued";

	@JsonIgnore private final Logger oLogger;
	@JsonIgnore private ServerWebSocket oSock;
	@JsonIgnore private final IEventResolver oResolver;
	@JsonIgnore private final IEventBus oLocalEventBus;
	@JsonProperty("host") private String oHost;
	@JsonProperty("status") private volatile Status oStatus;
	@JsonProperty("runId") private volatile Integer oRunId;
	@JsonProperty("benchTags") private List<String> oBenchTags;
	@JsonProperty("portalPort") private String oHostConfigPortalPort;

	public void setSocket(ServerWebSocket aSock){
		oSock = aSock;
		oSock.handler(this::handleIncomingBuffer);
		oSock.closeHandler(this::onConnectionClosed);
		var benchTagsStr = aSock.headers().get(PropertyKeys.BENCH_TAGS_KEY).strip();
		oBenchTags = benchTagsStr.isEmpty()
				? Collections.emptyList()
				: Arrays.asList(benchTagsStr.split(PropertyKeys.BENCH_TAG_DELIMITER));
		oHostConfigPortalPort = aSock.headers().get(PropertyKeys.CONFIG_PORTAL_PORT_KEY);
		oStatus = Status.IDLE;
	}

	private synchronized void onConnectionClosed(Void aVoid) {
		oLogger.error("Websocket disconnected");
		oStatus = Status.WEBSOCKET_DISCONNECTED;
		oSock = null;
		if(oRunId == null) return;
		DatabaseChangingEventBus.getInstance().push(new DeleteRunEvent(oRunId));
		oLogger.info(ACTIVE_RUN_QUEUE_DELETION_FMT, oRunId);
		oRunId = null;
	}

	public ServerTestRunner(String aHost) {
		super();
		oHost = aHost;
		oStatus = Status.WEBSOCKET_DISCONNECTED;
		oResolver = Util.testJsonResolver();
		oLogger = LoggerFactory.getLogger(oHost);
		oLocalEventBus = ConcurrentEventBus.builder()
				.addListener(TestRunnerFinishedEvent.class, this::onFinished)
				.build();
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
		DatabaseChangingEventBus.getInstance().push(trf);
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
			onConnectionClosed(null);
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
			onConnectionClosed(null);
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

	public boolean isIdle() {
		return oStatus.equals(Status.IDLE);
	}

	public Integer getRunId() {
		return oRunId;
	}

	public String getHost() {
		return oHost;
	}

	//used in debugging to force connection resets when trying to test robustness
	public void close() {
		oSock.close();
	}
	@Override

	public String toString() {
		return "ServerTestRunner{" +
				"oSock.remoteAddress()=" + oSock.remoteAddress() +
				", oStatus='" + oStatus + '\'' +
				", oBenchTags=" + oBenchTags +
				'}';
	}

	@Override
	public synchronized boolean accept(IResourceVisitor visitor) {
		return visitor.visit(this);
	}

	public boolean hasActiveSocket() {
		return oSock != null;
	}
}
