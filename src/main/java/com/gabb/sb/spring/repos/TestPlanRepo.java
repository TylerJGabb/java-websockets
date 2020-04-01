package com.gabb.sb.spring.repos;

import com.gabb.sb.spring.entities.TestPlan;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface TestPlanRepo extends CrudRepository<TestPlan, Integer> {


    /**
     * Count the number of runs in each test plan
     * that are IN_PROGRESS. If that count is less than tp.maxTestRunners
     * then that testplan's id is added to the list returned..
     * @return
     */
    @Query(
            "SELECT tp.id FROM TestPlan tp " +
                    "WHERE (" +
                    "SELECT COUNT(r.id) FROM Run r " +
                    "WHERE r.job.testPlan = tp " +
                    "AND r.status IN ('IN_PROGRESS') " +
                    ") < tp.maxTestRunners " +
                    "AND tp.isTerminated = false " +
                    "ORDER BY priority, lastProcessed ASC"
    )
    List<Integer> findActiveUnderRunnerCap();

    @Transactional
    @Modifying
    @Query("UPDATE TestPlan tp SET tp.status = 'IN_PROGRESS' WHERE tp.id = :tpId AND tp.status = 'NOT_STARTED_YET'")
    void setStatusInProgressIfNotStartedYet(@Param("tpId") Integer tpId);

    @Transactional
    @Modifying
    @Query(
    "UPDATE TestPlan tp " +
            "SET tp.status = CASE " + //set status to 'FAIL' if any failing jobs, else 'PASS'
                "WHEN (SELECT COUNT(j) FROM Job j WHERE j.testPlan.id = tp.id AND j.status = 'FAIL') > 0 THEN 'FAIL' " +
                "ELSE 'PASS' END, " +
            "tp.isTerminated = 1 " + //set the terminated flag to true
        //where no jobs remain for this tp that have not been terminated
        "WHERE (SELECT COUNT(j) FROM Job j WHERE j.testPlan.id = tp.id AND j.isTerminated = 0) = 0 " +
        //and this guy hasn't been terminated yet
        "AND tp.isTerminated = 0")
    void updateFinalStatus();

    @Query("SELECT tp.id FROM TestPlan tp WHERE tp.isTerminated = 0")
    List<Integer> findByIsTerminatedFalse();


}