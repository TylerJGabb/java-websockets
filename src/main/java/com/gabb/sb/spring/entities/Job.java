package com.gabb.sb.spring.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.architecture.Status;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "testPlanId")
    @JsonIgnore
    private TestPlan testPlan;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonProperty
    private List<Run> runs;

    @JsonProperty
    @Enumerated(EnumType.STRING)
    private Status status = Status.NOT_SET_YET;

    @Column(columnDefinition = "DATETIME(4)")
    private LocalDateTime lastRunStartedAt;

    private boolean isTerminated;
    private String benchTag;
    private int runCount;

    public Job() { }

    public Job(int runCount, TestPlan testPlan) {
        runs = new ArrayList<>();
        this.testPlan = testPlan;
        this.runCount = runCount;

    }

    public void addRun(Run run) {
        run.setJob(this);
        runs.add(run);
    }

    public void setStarted() {
        lastRunStartedAt = LocalDateTime.now();
        testPlan.setLastProcessed();
    }

    public boolean isTerminated() {
        return isTerminated;
    }

    public boolean isFailing() {
        return runs.stream().filter(Run::isFailing).count() > testPlan.getMaxAllowedFailures();
    }

    public boolean isPassing() {
        return runs.stream().filter(Run::isPassing).count() >= testPlan.getRequiredPasses();
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setTerminated(boolean val) {
        isTerminated = val;
    }

    public Integer getId() {
        return id;
    }
}
