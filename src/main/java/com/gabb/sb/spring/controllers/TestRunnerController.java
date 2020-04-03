package com.gabb.sb.spring.controllers;

import com.gabb.sb.ResourcePool;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/testRunners")
public class TestRunnerController {

    /**
     * If nameContains is specified as request parameter then attempts to find all testRunners with names
     * containing said string, otherwise returns all testRunners.
     */
    @ResponseBody
    @GetMapping
    private ResponseEntity<Object> findByNameContains(@RequestParam(name = "nameContains", required = false) String aNameContains){
        List<Object> resources = new ArrayList<>();
        if (aNameContains == null) {
            ResourcePool.getInstance().accept(resources::add);
        } else {
            ResourcePool.getInstance().accept(tr -> {
                if(tr.getName().toLowerCase().contains(aNameContains.toLowerCase())) resources.add(tr);
                return false;
            });
        }
        return ResponseEntity.ok(resources);
    }


}
