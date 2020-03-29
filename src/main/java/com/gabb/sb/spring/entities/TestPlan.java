package com.gabb.sb.spring.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
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
    private String status = "NOT IMPL YET";

    public TestPlan() { }

    public TestPlan(int jobCount){
        jobs = new ArrayList<>();
        for(int i = 0; i < jobCount; i++){
            jobs.add(new Job(2, this));
        }
    }
}
