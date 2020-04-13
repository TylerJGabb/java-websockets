package com.gabb.sb;

import com.gabb.sb.architecture.ServerTestRunner;
import com.gabb.sb.architecture.Status;
import com.gabb.sb.spring.entities.Job;
import com.gabb.sb.spring.entities.Run;
import com.gabb.sb.spring.repos.JobRepository;
import com.gabb.sb.spring.repos.RunRepo;
import com.gabb.sb.spring.repos.TestPlanRepo;

import java.util.List;

public class ResourceAllocationVisitor implements IResourceVisitor {

    private final Integer oPlanId;
    private JobRepository oJobRepository;
    private RunRepo oRunRepo;
    private TestPlanRepo oTestPlanRepo;

    public ResourceAllocationVisitor(Integer aPlanId, JobRepository aJobRepository, RunRepo aRunRepo,
                                     TestPlanRepo aTestPlanRepo) {

        oPlanId = aPlanId;
        oJobRepository = aJobRepository;
        oRunRepo = aRunRepo;
        oTestPlanRepo = aTestPlanRepo;
    }

    @Override
    public boolean visit(ServerTestRunner runner) {
        if (!runner.isIdle()) return false;
        List<Integer> jobIds;
        List<String> benchTags = runner.getBenchTags();
        jobIds = benchTags.isEmpty()
                ? oJobRepository.getForTestPlanWithoutBenchTags(oPlanId)
                : oJobRepository.getForTestPlanWithOrWithoutBenchTags(oPlanId, benchTags);
        if (!jobIds.isEmpty()) {
            Job job = oJobRepository.findById(jobIds.get(0)).orElseThrow();
            if (startRunReturnSuccessful(runner, job)) {
                oTestPlanRepo.setStatusInProgressIfNotStartedYet(oPlanId);
                return true;
            }
        }
        return false;
    }

    private boolean startRunReturnSuccessful(ServerTestRunner runner, Job job) {
        //add run to job
        Run run = new Run();
        run.setRunnerHost(runner.getHost());
        oRunRepo.save(run); //need to do this to get runId;
        //this call sets runner status, runId. Errors are handled internally. boolean is returned indicating success
        if(runner.startTestReturnSuccessful(run)) {
            run.setStatus(Status.IN_PROGRESS);
            job.addRun(run);
            job.setStarted(); //set job last started at (which sets tp last processed)
            oJobRepository.save(job); //save job, updates testplan too
            return true;
        } else {
            oRunRepo.delete(run);
            return false;
        }
    }
}
