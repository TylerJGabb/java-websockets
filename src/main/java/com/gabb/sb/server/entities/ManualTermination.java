package com.gabb.sb.server.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ManualTermination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private Integer id;

    @Column(columnDefinition = "DATETIME(4)")
    @JsonProperty
    private LocalDateTime submittedAt;

    @Column(columnDefinition = "DATETIME(4)")
    @JsonProperty
    private LocalDateTime processedAt;

    @JsonProperty
    private Integer testPlanId;

    //for serialization
    public ManualTermination() { }

    public ManualTermination(Integer testPlanId) {
        this.submittedAt = LocalDateTime.now();
        this.testPlanId = testPlanId;
    }

    public Integer getId() {
        return id;
    }

    public Integer getTestPlanId() {
        return testPlanId;
    }

    public void processed() {
        processedAt = LocalDateTime.now();
    }
}
