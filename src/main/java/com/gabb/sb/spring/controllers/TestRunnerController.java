package com.gabb.sb.spring.controllers;

import com.gabb.sb.ResourcePool;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/testRunners")
public class TestRunnerController {

    /**
     * If nameContains is specified as request parameter then attempts to find all testRunners with names
     * containing said string, otherwise returns all testRunners.
     */
    @ResponseBody
    @GetMapping
    @CrossOrigin
    private ResponseEntity<Object> findByNameContains(@RequestParam(name = "nameContains", required = false) String aNameContains){
        List<Object> resources = new ArrayList<>();
        if (aNameContains == null) {
            ResourcePool.getInstance().accept(r -> {
                resources.add(r);
                return false;
            });
        } else {
            ResourcePool.getInstance().accept(tr -> {
                if(tr.getHost().toLowerCase().contains(aNameContains.toLowerCase())) resources.add(tr);
                return false;
            });
        }
        return ResponseEntity.ok(resources);
    }


}
