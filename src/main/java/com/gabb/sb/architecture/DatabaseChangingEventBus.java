package com.gabb.sb.architecture;

import com.gabb.sb.architecture.events.IEvent;
import com.gabb.sb.architecture.events.bus.PrioritySyncEventBus;
import com.gabb.sb.architecture.events.concretes.DeleteRunEvent;
import com.gabb.sb.architecture.events.concretes.StartRunEvent;
import com.gabb.sb.architecture.events.concretes.TestRunnerFinishedEvent;
import com.gabb.sb.spring.entities.Job;
import com.gabb.sb.spring.entities.Run;
import com.gabb.sb.spring.repos.JobRepository;
import com.gabb.sb.spring.repos.RunRepo;
import com.gabb.sb.spring.repos.TestPlanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This event bus can potentially mutate database when processing events
 * Events are processed according to their {@link IEvent#getPriority()}
 */
@Component
public class DatabaseChangingEventBus extends PrioritySyncEventBus {

	private RunRepo runRepo;
	private TestPlanRepo testPlanRepo;
	private JobRepository jobRepository;

	private static DatabaseChangingEventBus oInstance;

	@Autowired
	public DatabaseChangingEventBus(
			TestPlanRepo testPlanRepo,
			JobRepository jobRepository,
			RunRepo runRepo) {

		super();
		this.testPlanRepo = testPlanRepo;
		this.jobRepository = jobRepository;
		this.runRepo = runRepo;
		addListener(TestRunnerFinishedEvent.class, this::testRunnerFinished);
		addListener(StartRunEvent.class, this::testStarted);
		addListener(DeleteRunEvent.class, this::deleteRun);
		oInstance = this;
	}

	private void deleteRun(DeleteRunEvent dre) {
		Run run = runRepo.findById(dre.getRunId()).orElse(null);
		if(run == null) return;
		run.orphan();
		runRepo.save(run);
		runRepo.delete(run);
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
		allocate();
	}

	private void allocate() {
		ResourcePool.getInstance().visitAll(runners -> {
			if(runners.size() == 0) return;
			List<Integer> testPlanIds = testPlanRepo.findActiveUnderRunnerCap();
			for(Integer planId: testPlanIds){
				for(ServerTestRunner runner : runners){
					List<Integer> jobIds;
					List<String> benchTags = runner.getBenchTags();
					jobIds = benchTags.isEmpty()
							? jobRepository.getForTestPlanWithoutBenchTags(planId)
							: jobRepository.getForTestPlanWithOrWithoutBenchTags(planId, benchTags);
					if(jobIds.isEmpty()) continue;
					Job job = jobRepository.findById(jobIds.get(0)).orElseThrow();
					if(startRunReturnSuccessful(runner, job)) return;
				}
			}
		}, ServerTestRunner::isIdle);
	}

	private boolean startRunReturnSuccessful(ServerTestRunner runner, Job job) {
		//add run to job
		Run run = new Run();
		run.setRunner(runner);
		runRepo.save(run); //need to do this to get runId;
		//this call sets runner status, runId. Errors are handled internally. boolean is returned indicating success
		if(runner.startTestReturnSuccessful(run)) {
			run.setStatus(Status.IN_PROGRESS);
			job.addRun(run);
			job.setStarted(); //set job last started at (which sets tp last processed)
			jobRepository.save(job); //save job, updates testplan too
			return true;
		} else {
			runRepo.delete(run);
			return false;
		}
	}

	private void testRunnerFinished(TestRunnerFinishedEvent aRunnerFinishedEvent){
		Integer runId = aRunnerFinishedEvent.runId;
		Status result = aRunnerFinishedEvent.result;
		Run run = runRepo.findById(runId).orElse(null);
		if(run == null){
			oLogger.error("Received outdated runId {}, run no longer exists in DB", runId);
			return;
		}
		Job job = run.getJob();
		if(job.isTerminated()) {
			oLogger.info("Run {} finished for terminated job {}, ignoring results", runId, job.getId());
			return;
		}
		run.setStatus(result);
		runRepo.save(run);
		boolean failing = job.isFailing();
		if(failing || job.isPassing()){
			job.setStatus(failing ? Status.FAIL : Status.PASS);
			job.setTerminated(true);
			jobRepository.save(job);
			ResourcePool resourcePool = ResourcePool.getInstance();
			var runsInProgress = runRepo.findInProgressForJob(job);
			runsInProgress.forEach(r -> {
				r.setStatus(Status.TERMINATED);
				resourcePool.terminate(r.getRunnerAddressToString());
				runRepo.save(r);
			});
		}
		oLogger.info("RunFinished: {} with result {}", runId, result);
	}
	
	private void testStarted(StartRunEvent aStartRunEvent){
		oLogger.info("DCEB MOCK handle of StartRunEvent for run {}", aStartRunEvent.runId);
	}

	public static DatabaseChangingEventBus getInstance() {
		if(oInstance == null) throw new IllegalStateException("Instance not set yet");
		return oInstance;
	}
}
