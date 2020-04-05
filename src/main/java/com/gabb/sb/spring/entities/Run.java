package com.gabb.sb.spring.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabb.sb.architecture.ServerTestRunner;
import com.gabb.sb.architecture.Status;

import javax.persistence.*;

@Entity
@JsonAutoDetect(
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.ANY
)
public class Run {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "jobId")
    @JsonIgnore
    private Job job;

    @Enumerated(EnumType.STRING)
    private Status status = Status.NOT_SET_YET;

    private String runnerAddressToString;

    public Run() { }

    public Run(Job job) {
        this.job = job;
    }

    public void orphan() {
        this.job = null;
    }

    public void setJob(Job job) {
        this.job = job;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public void setRunner(ServerTestRunner testRunner) {
        this.runnerAddressToString = testRunner.getAddress().toString();
    }

    public String getRunnerAddressToString() {
        return runnerAddressToString;
    }
    public Integer getId() {
        return id;
    }
    public Job getJob() {
        return job;
    }
    public boolean isFailing() {
        return status.equals(Status.FAIL);
    }
    public boolean isPassing() {
        return status.equals(Status.PASS);
    }
}
