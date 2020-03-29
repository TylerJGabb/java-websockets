package com.gabb.sb.spring.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
public class Run {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "jobId")
    private Job job;

    @JsonProperty
    private String status = "NOT IMPL YET";

    public Run() { }

    public Run(Job job) {
        this.job = job;
    }
}
