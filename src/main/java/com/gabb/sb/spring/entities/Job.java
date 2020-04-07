package com.gabb.sb.spring.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.architecture.Status;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@JsonAutoDetect(
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.ANY
)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "testPlanId")
    @JsonIgnore
    private TestPlan testPlan;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Run> runs;

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

    public boolean isTerminated() {
        return isTerminated;
    }
    public boolean isFailing() {
        return runs.stream().filter(Run::isFailing).count() > testPlan.getMaxAllowedFailures();
    }
    public boolean isPassing() {
        return runs.stream().filter(Run::isPassing).count() >= testPlan.getRequiredPasses();
    }
    public Integer getId() {
        return id;
    }
    public Status getStatus() {
        return status;
    }

    public void setStarted() {
        lastRunStartedAt = LocalDateTime.now();
        testPlan.setLastProcessed();
    }
    public void setTerminated(boolean val) {
        isTerminated = val;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
}
