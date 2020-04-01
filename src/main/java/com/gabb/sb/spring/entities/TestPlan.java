package com.gabb.sb.spring.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.architecture.Status;
import com.gabb.sb.spring.controllers.TestPlanPostDTO;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TestPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private Integer id;

    @OneToMany(mappedBy = "testPlan", cascade = CascadeType.ALL)
    @JsonProperty
    private List<Job> jobs;

    @JsonProperty
    @Enumerated(EnumType.STRING)
    private Status status = Status.NOT_STARTED_YET;

    @Column(columnDefinition = "DATETIME(4)")
    private LocalDateTime lastProcessed;

    public Status getStatus() {
        return status;
    }

    @JsonProperty
    private Integer priority;
    private boolean isTerminated;
    private Integer maxTestRunners;
    private Integer maxAllowedFailures;
    private Integer requiredPasses;

    public TestPlan() { }

    public TestPlan(TestPlanPostDTO body) {
        this.maxAllowedFailures = body.maximumAllowedFailures;
        this.requiredPasses = body.requiredPasses;
        this.maxTestRunners = body.maxTestRunners;
        this.priority = body.priority;
//        this.buildName = body.buildName;
//        this.tags = body.tags;
//        this.ignore = body.ignoredTags;

        int runCount = maxAllowedFailures + requiredPasses;
        jobs = new ArrayList<>();
        jobs.add(new Job(runCount, this));
        jobs.add(new Job(runCount, this));
    }

    public TestPlan(int jobCount){
        maxTestRunners = 4;
        priority = 1;
        requiredPasses = 1;
        maxAllowedFailures = 3;
        jobs = new ArrayList<>();
        for(int i = 0; i < jobCount; i++){
            jobs.add(new Job(4, this));
        }
    }

    public void setLastProcessed() {
        lastProcessed = LocalDateTime.now();
    }

    public Integer getMaxAllowedFailures() {
        return maxAllowedFailures;
    }

    public Integer getRequiredPasses() {
        return requiredPasses;
    }

    public Integer getId() {
        return id;
    }

    public boolean isTerminated() {
        return isTerminated;
    }

    public void setTerminated(boolean val) {
        isTerminated = val;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
