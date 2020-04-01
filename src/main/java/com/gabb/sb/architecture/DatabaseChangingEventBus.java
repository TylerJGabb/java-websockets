package com.gabb.sb.architecture;

import com.gabb.sb.GuardedResourcePool;
import com.gabb.sb.architecture.events.IEvent;
import com.gabb.sb.architecture.events.bus.PrioritySyncEventBus;
import com.gabb.sb.architecture.events.concretes.DeleteRunEvent;
import com.gabb.sb.architecture.events.concretes.TestRunnerFinishedEvent;
import com.gabb.sb.spring.entities.Job;
import com.gabb.sb.spring.entities.ManualTermination;
import com.gabb.sb.spring.entities.Run;
import com.gabb.sb.spring.entities.TestPlan;
import com.gabb.sb.spring.repos.JobRepository;
import com.gabb.sb.spring.repos.ManualTerminationRepo;
import com.gabb.sb.spring.repos.RunRepo;
import com.gabb.sb.spring.repos.TestPlanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.gabb.sb.Loggers.DATABASE_LOGGER;

/**
 * This event bus can potentially mutate database when processing events
 */
@Component
public class DatabaseChangingEventBus extends PrioritySyncEventBus {

	private final ManualTerminationRepo termRepo;
	private final RunRepo runRepo;
	private final TestPlanRepo testPlanRepo;
	private final JobRepository jobRepository;

	private static DatabaseChangingEventBus oInstance;

	public static DatabaseChangingEventBus getInstance() {
		if (oInstance != null) return oInstance;
		throw new IllegalStateException("Instance not set yet");
	}

	@Autowired
	public DatabaseChangingEventBus(
			TestPlanRepo testPlanRepo,
			JobRepository jobRepository,
			RunRepo runRepo,
			ManualTerminationRepo termRepo) {

		super();
		this.testPlanRepo = testPlanRepo;
		this.jobRepository = jobRepository;
		this.runRepo = runRepo;
		this.termRepo = termRepo;
		addListener(TestRunnerFinishedEvent.class, this::testRunnerFinished);
		addListener(DeleteRunEvent.class, this::deleteRun);
		oInstance = this;
	}

	@Override
	protected boolean restartUponDeath() {
		return true;
	}

	@Override
	protected int getProcessingPeriodMillis() {
		return 500;
	}

	@Override
	protected final boolean handleExceptionReturnWhetherToStopProcessing(Throwable aThrown) {
		oLogger.error("ERROR IN DCEB", aThrown);
		return false;
	}

	@Override
	protected final void beforeProcessing() {
		processManualTerminations();
	}

	@Override
	protected final void afterProcessing() {
		testPlanStatusUpdate();
		allocate();
	}

	private void testPlanStatusUpdate() {
		List<Integer> nonTerminatedIds = testPlanRepo.findByIsTerminatedFalse();
		testPlanRepo.updateFinalStatus();
		for(TestPlan tp : testPlanRepo.findAllById(nonTerminatedIds)){
			if(tp.isTerminated()){
				DATABASE_LOGGER.info("Marked final status of TestPlan {} to {}", tp.getId(), tp.getStatus());
			}
		}
	}

	private void processManualTerminations() {
		//the user submits a stop request to the api layer, something line /api/testPlans/stop?id=4
		//maybe manualStop column?
		//maybe new table, stop request, 1 to 1 mapping between stop request and test plan id
		//stop request can have further attributes

		Set<ManualTermination> terminations = termRepo.findByProcessedAtIsNull();
		for(ManualTermination term : terminations){
			//mark tp as terminated immediately
			//find all runs in progress and terminate them
			//find all jobs in progress and terminate them
			Integer testPlanId = term.getTestPlanId();
			term.processed();
			termRepo.save(term);
			TestPlan tp = testPlanRepo.findById(testPlanId).orElse(null);
			if(tp == null) continue;
			oLogger.info("Processing manual termination for tpId={}", tp.getId());
			tp.setTerminated(true);
			tp.setStatus(Status.TERMINATED);
			testPlanRepo.save(tp);
			Set<Run> inProgress = runRepo.findInProgressForTestPlan(testPlanId);
			if(inProgress.isEmpty()) continue;
			inProgress.forEach(r -> {
				r.setStatus(Status.TERMINATED);
				GuardedResourcePool.getInstance().sendTerminationSignal(r.getRunnerAddressToString());
				r.getJob().setStatus(Status.TERMINATED);
				runRepo.save(r);
				Job job = jobRepository.findById(r.getJob().getId()).orElseThrow();
				job.setStatus(Status.TERMINATED);
				jobRepository.save(job);
			});
		}

	}

	private void deleteRun(DeleteRunEvent dre) {
		Run run = runRepo.findById(dre.getRunId()).orElse(null);
		if(run == null) return;
		run.orphan();
		runRepo.save(run);
		runRepo.delete(run);
	}

	private void allocate(){
		List<Integer> testPlanIds = testPlanRepo.findActiveUnderRunnerCap();
		for(Integer planId: testPlanIds){
			GuardedResourcePool.getInstance().applyForEachAfterFiltering(ServerTestRunner::isIdle, runner -> {
				List<Integer> jobIds;
				List<String> benchTags = runner.getBenchTags();
				jobIds = benchTags.isEmpty()
						? jobRepository.getForTestPlanWithoutBenchTags(planId)
						: jobRepository.getForTestPlanWithOrWithoutBenchTags(planId, benchTags);
				if(!jobIds.isEmpty()) {
					Job job = jobRepository.findById(jobIds.get(0)).orElseThrow();
					if(startRunReturnSuccessful(runner, job)){
						testPlanRepo.setStatusInProgressIfNotStartedYet(planId);
						return true;
					}
				}
				return false;
			});
		}
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
			var runsInProgress = runRepo.findInProgressForJob(job);
			runsInProgress.forEach(r -> {
				r.setStatus(Status.TERMINATED);
				GuardedResourcePool.getInstance().sendTerminationSignal(r.getRunnerAddressToString());
				runRepo.save(r);
			});
			oLogger.info("Job {} finished with status {}", job.getId(), job.getStatus());
		} else {
			oLogger.info("Run {} finished with result {}", runId, result);
		}
	}
}
