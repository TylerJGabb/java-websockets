package com.gabb.sb.spring.controllers;

import com.gabb.sb.GuardedResourcePool;
import com.gabb.sb.architecture.ServerTestRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static com.gabb.sb.Loggers.CONTROLLER_LOGGER;

@Controller
@RequestMapping("/testRunners")
public class TestRunnerController {

    /**
     * If nameContains is specified as request parameter then attempts to find a testRunner with a name
     * containing aNameContains, otherwise, returns all testRunners.
     * @param aNameContains
     * @return
     */
    @ResponseBody
    @GetMapping
    private ResponseEntity<Object> getByNameOrGetAll(@RequestParam(name = "nameContains", required = false) String aNameContains){
        CONTROLLER_LOGGER.info("nameContains={}", aNameContains);
        if(aNameContains == null) return ResponseEntity.ok(GuardedResourcePool.getInstance().getListCopy());
        Predicate<ServerTestRunner> predicate = tr -> tr.getName().toLowerCase().contains(aNameContains.toLowerCase());
        var found = new AtomicReference<ServerTestRunner>();
        GuardedResourcePool.getInstance().findFirstAndConsume(predicate, found::set);
        if(found.get() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(found);
    }


}
