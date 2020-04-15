package com.gabb.sb.server.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.gabb.sb.Status;
import com.gabb.sb.server.controllers.TestPlanPostDTO;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@JsonAutoDetect(
        isGetterVisibility = JsonAutoDetect.Visibility.NONE, // 'is' like 'isTerminated'
        getterVisibility = JsonAutoDetect.Visibility.NONE,  //  regular getters
        fieldVisibility = JsonAutoDetect.Visibility.ANY)

public class TestPlan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "testPlan", cascade = CascadeType.ALL)
    private List<Job> jobs;

    @Enumerated(EnumType.STRING)
    private Status status = Status.NOT_STARTED_YET;

    @Column(columnDefinition = "DATETIME(4)")
    private LocalDateTime lastProcessed;

    private boolean isTerminated;
    private Integer priority;
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

    public void setLastProcessed() {
        lastProcessed = LocalDateTime.now();
    }
    public void setTerminated(boolean val) {
        isTerminated = val;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() { return status; }
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
}
