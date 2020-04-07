package com.gabb.sb.spring.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.gabb.sb.spring.entities.ManualTermination;
import com.gabb.sb.spring.entities.TestPlan;
import com.gabb.sb.spring.repos.ManualTerminationRepo;
import com.gabb.sb.spring.repos.TestPlanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.gabb.sb.Loggers.CONTROLLER_LOGGER;

@Controller
@RequestMapping("api/testPlans")
public class TestPlanController {

    private final ManualTerminationRepo termRepo;
    private final TestPlanRepo planRepo;
    private ObjectWriter objectWriter;

    @Autowired
    public TestPlanController(TestPlanRepo planRepo, ManualTerminationRepo termRepo) {
        this.planRepo = planRepo;
        this.termRepo = termRepo;
        objectWriter = new ObjectMapper().writer();
    }

    @GetMapping
    @ResponseBody
    private ResponseEntity<Object> get(){
        return new ResponseEntity<>(planRepo.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @ResponseBody
    private ResponseEntity<Object> getById(@PathVariable("id") Integer id){
        return new ResponseEntity<>(planRepo.findById(id), HttpStatus.OK);
    }

    @PostMapping
    private ResponseEntity<Integer> post(@RequestBody TestPlanPostDTO body) throws JsonProcessingException {
        Integer id = planRepo.save(new TestPlan(body)).getId();
        CONTROLLER_LOGGER.info("New test plan id={} added: {}", id, objectWriter.writeValueAsString(body));
        System.out.println();
        return ResponseEntity.ok(id);
    }

    @PostMapping("/stop/{id}")
    private ResponseEntity<Object> stopById(@PathVariable("id") Integer id){
        Optional<TestPlan> tp = planRepo.findById(id);
        if(tp.isEmpty()) return ResponseEntity.notFound().build();
        if(tp.get().isTerminated()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Aready Terminated");
        CONTROLLER_LOGGER.info("Manual Termination submitted for TestPlan {}", id);
        return ResponseEntity.ok(termRepo.save(new ManualTermination(id)).getId());
    }
}
