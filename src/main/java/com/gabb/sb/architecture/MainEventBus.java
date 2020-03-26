package com.gabb.sb.architecture;

import com.gabb.sb.architecture.events.bus.EventBus;
import com.gabb.sb.architecture.events.bus.IEventBus;
import com.gabb.sb.architecture.events.concretes.StartTestEvent;
import com.gabb.sb.architecture.events.concretes.TestRunnerFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainEventBus extends EventBus {
	
	private static IEventBus oInstance;
	private Logger oLogger;
	
	private MainEventBus(){
		oLogger = LoggerFactory.getLogger(this.getClass());
		addListener(TestRunnerFinishedEvent.class, this::testRunnerFinished);
		addListener(StartTestEvent.class, this::testStarted);
	}
	
	private void testRunnerFinished(TestRunnerFinishedEvent aRunnerFinishedEvent){
		oLogger.info("MainEventBus mock handle of TestRunnerFinishedEvent for run {}", aRunnerFinishedEvent.runId);
	}
	
	private void testStarted(StartTestEvent aStartTestEvent){
		oLogger.info("MainEventBus mock handle of StartTestEvent for run {}", aStartTestEvent.runId);
	}

	public static IEventBus getInstance() {
		if(oInstance == null){
			synchronized (MainEventBus.class){
				if(oInstance == null){
					oInstance = new MainEventBus();			
				}
			}
		}
		return oInstance;
	}
}
