package com.gabb.sb.spring.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
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
    private TestPlan testPlan;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonProperty
    private List<Run> runs;

    @JsonProperty
    private String status = "NOT IMPL YET";

    public Job() { }

    public Job(int runCount, TestPlan testPlan) {

        this.testPlan = testPlan;
        runs = new ArrayList<>();
        for(int i = 0; i < runCount; i++){
            runs.add(new Run(this));
        }
    }
}
