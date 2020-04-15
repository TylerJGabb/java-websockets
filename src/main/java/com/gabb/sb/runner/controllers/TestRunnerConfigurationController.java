package com.gabb.sb.runner.controllers;

import com.gabb.sb.runner.TestExecutor;
import com.gabb.sb.runner.TestRunnerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("api/config")
public class TestRunnerConfigurationController {

    @Autowired
    TestExecutor oTestExecutor;

    @CrossOrigin
    @PutMapping("/down")
    public ResponseEntity<String> down() {
        if(!oTestExecutor.isUp()) return new ResponseEntity<>("WebSocket is already down", HttpStatus.BAD_REQUEST);
        oTestExecutor.stopWebSocket();
        return new ResponseEntity<>("good", HttpStatus.OK);
    }

    @CrossOrigin
    @PutMapping("/up")
    public ResponseEntity<String> up() {
        if (oTestExecutor.isUp()) return new ResponseEntity<>("WebSocket is already up", HttpStatus.BAD_REQUEST);
        oTestExecutor.startWebSocket(
                TestRunnerConfig.getInstance().getServerPort(),
                TestRunnerConfig.getInstance().getServerHost(),
                "/");
        return new ResponseEntity<>("good", HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping
    public ResponseEntity<Object> getConfig(){
        return new ResponseEntity<>(TestRunnerConfig.getInstance(), HttpStatus.OK);
    }
}
