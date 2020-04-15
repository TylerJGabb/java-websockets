package com.gabb.sb.server;

import com.gabb.sb.server.resourcepool.FreeResourceVisitor;
import com.gabb.sb.server.resourcepool.ResourceAllocationVisitor;
import com.gabb.sb.server.resourcepool.ResourcePool;
import com.gabb.sb.Status;
import com.gabb.sb.events.bus.PrioritySyncEventBus;
import com.gabb.sb.events.concretes.DeleteRunEvent;
import com.gabb.sb.events.concretes.TestRunnerFinishedEvent;
import com.gabb.sb.server.entities.Job;
import com.gabb.sb.server.entities.ManualTermination;
import com.gabb.sb.server.entities.Run;
import com.gabb.sb.server.entities.TestPlan;
import com.gabb.sb.server.repos.JobRepository;
import com.gabb.sb.server.repos.ManualTerminationRepo;
import com.gabb.sb.server.repos.RunRepo;
import com.gabb.sb.server.repos.TestPlanRepo;
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
		addListener(TestRunnerFinishedEvent.class, runnerFinishedEvent -> testRunnerFinished(runnerFinishedEvent));
		addListener(DeleteRunEvent.class, dre -> deleteRun(dre));
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

	private void allocate() {
		List<Integer> testPlanIds = testPlanRepo.findActiveUnderRunnerCap();
		for(Integer planId: testPlanIds) {
			boolean allocated = ResourcePool.getInstance().accept(
					new ResourceAllocationVisitor(planId, jobRepository, runRepo, testPlanRepo));
			if(allocated) return;
		}
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
		Set<ManualTermination> terminations = termRepo.findByProcessedAtIsNull();
		for(ManualTermination term : terminations){
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
				ResourcePool.getInstance().accept(new FreeResourceVisitor(r.getRunnerHost()));
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
				ResourcePool.getInstance().accept(new FreeResourceVisitor(r.getRunnerHost()));
				runRepo.save(r);
			});
			oLogger.info("Job {} finished with status {}", job.getId(), job.getStatus());
		} else {
			oLogger.info("Run {} finished with result {}", runId, result);
		}
	}
}
