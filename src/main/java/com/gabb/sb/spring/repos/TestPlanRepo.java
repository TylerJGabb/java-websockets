package com.gabb.sb.spring.repos;

import com.gabb.sb.spring.entities.TestPlan;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TestPlanRepo extends CrudRepository<TestPlan, Integer> {


    /**
     * Count the number of runs in each test plan
     * that are IN_PROGRESS. If that count is less that testplan.rule.maximumRunners
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

}