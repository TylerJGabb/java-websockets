package com.gabb.sb.architecture;

import com.gabb.sb.architecture.events.IEvent;
import com.gabb.sb.architecture.events.bus.PrioritySyncEventBus;
import com.gabb.sb.architecture.events.concretes.StartRunEvent;
import com.gabb.sb.architecture.events.concretes.TestRunnerFinishedEvent;

import java.util.Random;

/**
 * This event bus can potentially mutate database when processing events
 * Events are processed according to their {@link IEvent#getPriority()}
 */
public class DatabaseChangingEventBus extends PrioritySyncEventBus {
	
	private static DatabaseChangingEventBus oInstance;
	
	private DatabaseChangingEventBus(){
		super();
		addListener(TestRunnerFinishedEvent.class, this::testRunnerFinished);
		addListener(StartRunEvent.class, this::testStarted);
	}

	@Override
	protected boolean handleException(Throwable aThrown) {
		oLogger.error("ERROR IN DCEB", aThrown);
		return false;
	}
	
	@Override
	protected void beforeProcessing() {
		oLogger.trace("DCEB ProcessManualTerminations");
	}

	@Override
	protected void afterProcessing() {
		oLogger.trace("DCEB TestPlanStatusUpdate");
		ResourcePool.getInstance().visit(tr -> {
			if("IDLE".equals(tr.getStatus())) {
				Run run = new Run(new Random().nextInt());
				tr.startTest(run);
				oLogger.info("DCEB Resource Allocation: Started Run {} on {}", run.getId(), tr);
				return true;
			}
			return false;
		});
	}

	private void testRunnerFinished(TestRunnerFinishedEvent aRunnerFinishedEvent){
		oLogger.info("DCEB MOCK TestRunnerFinishedEvent for run {}", aRunnerFinishedEvent.runId);
	}
	
	private void testStarted(StartRunEvent aStartRunEvent){
		oLogger.info("DCEB MOCK handle of StartRunEvent for run {}", aStartRunEvent.runId);
	}

	public static DatabaseChangingEventBus getInstance() {
		if(oInstance == null){
			synchronized (DatabaseChangingEventBus.class){
				if(oInstance == null){
					oInstance = new DatabaseChangingEventBus();			
				}
			}
		}
		return oInstance;
	}
}
