package com.gabb.sb.spring.controllers;

import com.gabb.sb.spring.entities.TestPlan;
import com.gabb.sb.spring.repos.TestPlanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/testPlans")
public class TestPlanController {

    TestPlanRepo repo;

    @Autowired
    public TestPlanController(TestPlanRepo repo) {
        this.repo = repo;
    }

    @GetMapping
    @ResponseBody
    private ResponseEntity<Object> get(){
        return new ResponseEntity<>(repo.findAll(), HttpStatus.OK);
    }

    @GetMapping("/testPlans/{id}")
    @ResponseBody
    private ResponseEntity<Object> getById(@PathVariable("id") Integer id){
        return new ResponseEntity<>(repo.findById(id), HttpStatus.OK);
    }

    @PostMapping
    private ResponseEntity<Integer> post(@RequestBody TestPlanPostDTO body){
        return ResponseEntity.ok(repo.save(new TestPlan(body)).getId());
    }
}
