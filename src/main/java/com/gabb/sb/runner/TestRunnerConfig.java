package com.gabb.sb.runner;

import com.gabb.sb.server.ServerSpringBootApplication;

import java.util.Collections;

public class TestRunnerConfig {

    private static TestRunnerConfig oInstance;

    private String[] oBenchTags;
    private int oServerPort;
    private String oServerHost;

    public static TestRunnerConfig getInstance(){
        if(oInstance == null){
            oInstance = new TestRunnerConfig();
        }
        return oInstance;
    }

    //todo read from default config
    public TestRunnerConfig() {
        oBenchTags = new String[0];
        oServerPort = ServerSpringBootApplication.PORT;
        oServerHost = ServerSpringBootApplication.HOST;
    }

    public String[] getBenchTags() {
        return oBenchTags;
    }

    public int getServerPort() {
        return oServerPort;
    }

    public String getServerHost() {
        return oServerHost;
    }
}
