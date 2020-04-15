package com.gabb.sb.server.repos;

import com.gabb.sb.server.entities.Job;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends CrudRepository<Job, Integer> {

    /**
     * Get jobs with totalRuns < runCount, ordered by last processed
     * that are compatible with the given allowedTags or have no tag
     */
    @Query(
            "SELECT j.id from Job j " +
                    "WHERE (SELECT COUNT(r) from Run r WHERE r.job = j) < j.runCount " +
                    "AND j.testPlan.id = :testPlanId " +
                    "AND (j.benchTag IN (:allowedTags) OR j.benchTag IS NULL) " +
                    "AND j.isTerminated = false " +
                    "ORDER BY j.lastRunStartedAt ASC"
    )
    List<Integer> getForTestPlanWithOrWithoutBenchTags(
            @Param("testPlanId") Integer testPlanId,
            @Param("allowedTags") List<String> allowedTags
    );

    /**
     * Get jobs with totalRuns < runCount, ordered by last processed
     * that have no benchTag
     */
    @Query(
            "SELECT j.id from Job j " +
                    "WHERE (SELECT COUNT(r) from Run r WHERE r.job = j) < j.runCount " +
                    "AND j.testPlan.id = :testPlanId " +
                    "AND j.benchTag IS NULL " +
                    "AND j.isTerminated = false " +
                    "ORDER BY j.lastRunStartedAt ASC"
    )
    List<Integer> getForTestPlanWithoutBenchTags(@Param("testPlanId") Integer testPlanId);

}