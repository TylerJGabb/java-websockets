package com.gabb.sb.spring.controllers;

import com.gabb.sb.spring.repos.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/jobs")
public class JobController {

    JobRepository repo;

    @Autowired
    public JobController(JobRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    @ResponseBody
    private ResponseEntity<Object> get(){
        return new ResponseEntity<>(repo.findAll(), HttpStatus.OK);
    }

    @GetMapping("/jobs/{id}")
    @ResponseBody
    private ResponseEntity<Object> getById(@PathVariable("id") Integer id){
        return new ResponseEntity<>(repo.findById(id), HttpStatus.OK);
    }

}
